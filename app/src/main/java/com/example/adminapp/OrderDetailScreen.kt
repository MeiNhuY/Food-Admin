package com.example.adminapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adminapp.Domain.FoodModel
import com.example.adminapp.Domain.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(orderId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val order = remember { mutableStateOf<OrderModel?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val isAuthorized =
        remember { mutableStateOf(true) } // mặc định true nếu đã xác thực ở màn trước
    val repository = remember { MainRepository() }
    val dbRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId)


    LaunchedEffect(orderId) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            repository.checkIfUserIsAdmin { isAdmin ->
                if (isAdmin) {
                    val dbRef = FirebaseDatabase.getInstance().getReference("Order").child(orderId)
                    dbRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                order.value = snapshot.getValue(OrderModel::class.java)
                            } else {
                                Toast.makeText(context, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show()
                            }
                            isLoading.value = false
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Không tải được đơn hàng", Toast.LENGTH_SHORT).show()
                            isLoading.value = false
                        }
                    })
                } else {
                    isAuthorized.value = false
                    isLoading.value = false
                }
            }
        } else {
            isAuthorized.value = false
            isLoading.value = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi Tiết Đơn Hàng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading.value -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            !isAuthorized.value -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Bạn không có quyền truy cập trang này.")
            }

            order.value != null -> OrderDetailContent(order = order.value!!, padding = padding)
        }
    }
}


@Composable
fun UpdateOrderStatusButton(orderId: String) {
    val context = LocalContext.current

    // Create a list of statuses
    val statuses = listOf("Chờ xác nhận", "Đã xác nhận", "Đang giao hàng", "Đã Giao Hàng Thành Công")
    var selectedStatus by remember { mutableStateOf(statuses[0]) }

    // Keep track of dropdown visibility
    var expanded by remember { mutableStateOf(false) }

    // Create an instance of the MainRepository
    val repository = remember { MainRepository() }

    // Dropdown to choose the status
    Column(modifier = Modifier.fillMaxWidth()) {
        // Button to open the dropdown menu
        Button(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Chọn trạng thái")
        }

        // Show the dropdown menu when expanded is true
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(text = status) },
                    onClick = {
                        selectedStatus = status
                        repository.updateOrderStatusInDatabase(orderId, selectedStatus)
                        expanded = false
                    }
                )
            }
        }
    }
}




@Composable
fun OrderDetailContent(order: OrderModel, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(padding)
    ) {
        OrderInfoRow("Mã đơn hàng:", order.orderId)
        OrderInfoRow("Tên khách hàng:", order.userName)
        OrderInfoRow("Địa chỉ giao hàng:", order.address)
        OrderInfoRow("Ngày đặt:", formatTimestamp(order.timestamp))
        OrderInfoRow("Phương thức thanh toán:", order.paymentMethod)

        Spacer(modifier = Modifier.height(16.dp))

        OrderInfoRow("Trạng thái:", order.status)

        // Nút cập nhật trạng thái
        UpdateOrderStatusButton(orderId = order.orderId)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Danh sách món:", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        if (order.items.isEmpty()) {
            Text("Không có món trong đơn hàng này.", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(order.items) { item ->
                    ProductRow(item)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tổng cộng: ${order.totalPrice} VND", fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}


@Composable
fun OrderInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}

@Composable
fun ProductRow(item: FoodModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.Title, fontWeight = FontWeight.Bold)
                Text(text = "x${item.numberInCart} • ${item.Price} VND", fontSize = 13.sp)
            }
            Text(
                text = "${item.numberInCart * item.Price} VND",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
