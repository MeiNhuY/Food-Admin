package com.example.adminapp

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adminapp.Domain.FoodModel
import com.example.adminapp.Domain.OrderModel
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.BarChart // Thêm dòng này vào phần import
import androidx.navigation.NavController


@Composable
fun RevenueScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderModel?>(null) } // Lưu đơn hàng đã chọn
    val orders = remember { mutableStateOf<List<OrderModel>>(emptyList()) }

    // Fetch orders from Firebase
    LaunchedEffect(Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Order")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<OrderModel>()
                for (child in snapshot.children) {
                    val order = child.getValue(OrderModel::class.java)
                    if (order != null) {
                        list.add(order)
                    }
                }
                orders.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val selectedDateStr = dateFormat.format(selectedDate)

    val filteredOrders = orders.value.filter {
        val orderDate = Date(it.timestamp)
        dateFormat.format(orderDate) == selectedDateStr
    }

    val totalRevenue = filteredOrders.sumOf { it.totalPrice }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Doanh thu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Chọn ngày"
                )
            }
            IconButton(onClick = {
                navController.navigate("chart")
                // Chuyển đến màn hình biểu đồ - ví dụ gọi hàm điều hướng
                // TODO: Gọi NavController điều hướng tới ChartScreen
            }) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Biểu đồ doanh thu"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ngày đã chọn: $selectedDateStr",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Tổng doanh thu ngày $selectedDateStr", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${"%,.0f".format(totalRevenue)}đ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Giao dịch trong ngày", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredOrders.isEmpty()) {
            Text("Không có giao dịch nào trong ngày này.")
        } else {
            filteredOrders.sortedByDescending { it.timestamp }.forEach { order ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeStr = timeFormat.format(Date(order.timestamp))
                RecentTransactionItem(
                    time = "Lúc $timeStr",
                    amount = "${"%,.0f".format(order.totalPrice)}đ",
                    onClick = { selectedOrder = order } // Khi nhấn vào giao dịch, lưu lại đơn hàng đã chọn
                )
            }
        }

        // Hiển thị chi tiết đơn hàng nếu có
        selectedOrder?.let { order ->
            DetailOrderDialog(order = order, onDismiss = { selectedOrder = null })
        }
    }

    // Màn hình chọn ngày
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth)
                if (newCal.time <= Calendar.getInstance().time) {
                    selectedDate = newCal.time
                }
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = Calendar.getInstance().timeInMillis
            show()
        }
    }
}

@Composable
fun RecentTransactionItem(time: String, amount: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }, // Thêm sự kiện nhấn vào item
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(time)
            Text(amount, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DetailOrderDialog(order: OrderModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chi tiết đơn hàng") },
        text = {
            Column {
                // Hiển thị các thông tin chung của đơn hàng
                Text("Mã đơn: ${order.orderId}")
                Text("Ngày: ${SimpleDateFormat("dd/MM/yyyy").format(Date(order.timestamp))}")
                Text("Tổng tiền: ${"%,.0f".format(order.totalPrice)}đ")
                Text("Phương thức thanh toán: ${order.paymentMethod}")
                Text("Trạng thái: ${order.status}")

                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị chi tiết các sản phẩm trong đơn hàng
                Text("Danh sách sản phẩm trong đơn hàng:")
                order.items.forEach { food ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Sản phẩm: ${food.Title}")
                        Text("Mô tả: ${food.Description}")
                        Text("Giá: ${"%,.0f".format(food.Price)}đ")
                        Text("Số lượng: ${food.numberInCart}")
                        Text("Tổng giá: ${"%,.0f".format(food.Price * food.numberInCart)}đ")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}


//@Preview(showBackground = true)
//@Composable
//fun RevenueScreenPreview() {
//    RevenueScreen()
//}
