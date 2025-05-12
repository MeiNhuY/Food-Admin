package com.example.adminapp.ui.theme//package com.example.adminapp.ui.theme
//
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.google.firebase.database.*
//import com.example.adminapp.Domain.OrderModel
//import com.example.adminapp.Domain.FoodModel
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun RevenueOverviewScreen(onChartClicked: () -> Unit) {
//    val orders = remember { mutableStateOf<List<OrderModel>>(emptyList()) }
//
//    LaunchedEffect(Unit) {
//        // Lấy dữ liệu từ Firebase
//        val dbRef = FirebaseDatabase.getInstance().getReference("Order")
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val list = mutableListOf<OrderModel>()
//                for (child in snapshot.children) {
//                    val order = child.getValue(OrderModel::class.java)
//                    if (order != null) {
//                        list.add(order)
//                    }
//                }
//                orders.value = list
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//    }
//
//    val totalRevenue = orders.value.sumOf { it.totalPrice }
//    val totalOrders = orders.value.size
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .background(Color(0xFFF5F5F5))
//    ) {
//        Text(
//            text = "Tổng quan",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
//            elevation = CardDefaults.cardElevation(8.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(modifier = Modifier.padding(24.dp)) {
//                Text("Tổng doanh thu", color = Color.White)
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "${"%,.0f".format(totalRevenue)}đ",
//                    style = MaterialTheme.typography.headlineMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
//            elevation = CardDefaults.cardElevation(8.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(modifier = Modifier.padding(24.dp)) {
//                Text("Số lượng đơn hàng", color = Color.White)
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "$totalOrders đơn",
//                    style = MaterialTheme.typography.headlineMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Nút để chuyển đến màn hình biểu đồ
//        Button(
//            onClick = onChartClicked,
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
//        ) {
//            Text("Xem Biểu đồ doanh thu", color = Color.White)
//        }
//    }
//}
//
//@Composable
//fun RevenueChartScreen() {
//    var selectedFilter by remember { mutableStateOf("Ngày") }
//    var expanded by remember { mutableStateOf(false) }
//
//    val filters = listOf("Ngày", "Tuần", "Tháng")
//    val orders = remember { mutableStateOf<List<OrderModel>>(emptyList()) }
//
//    LaunchedEffect(Unit) {
//        // Lấy dữ liệu từ Firebase
//        val dbRef = FirebaseDatabase.getInstance().getReference("Order")
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val list = mutableListOf<OrderModel>()
//                for (child in snapshot.children) {
//                    val order = child.getValue(OrderModel::class.java)
//                    if (order != null) {
//                        list.add(order)
//                    }
//                }
//                orders.value = list
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//    }
//
//    val revenue = calculateRevenueByFilter(orders.value, selectedFilter)
//    val chartData = generateChartData(orders.value, selectedFilter)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .background(Color(0xFFF5F5F5))
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Biểu đồ doanh thu",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            Box {
//                Text(
//                    text = selectedFilter,
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(8.dp))
//                        .clickable { expanded = true }
//                        .background(Color.White)
//                        .padding(horizontal = 12.dp, vertical = 8.dp),
//                    color = Color.Black
//                )
//
//                DropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false }
//                ) {
//                    filters.forEach { filter ->
//                        DropdownMenuItem(
//                            text = { Text(filter) },
//                            onClick = {
//                                selectedFilter = filter
//                                expanded = false
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        RevenueBarChart(data = chartData)
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Text("Giao dịch gần đây", fontWeight = FontWeight.SemiBold)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        orders.value
//            .sortedByDescending { it.timestamp }
//            .take(3)
//            .forEach {
//                val date = SimpleDateFormat("dd/MM").format(Date(it.timestamp))
//                RecentTransactionItem("Ngày $date", "${"%,.0f".format(it.totalPrice)}đ")
//            }
//    }
//}
//
//@Composable
//fun RevenueBarChart(data: List<Pair<String, Double>>) {
//    val maxRevenue = data.maxOfOrNull { it.second } ?: 1.0
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp),
//        verticalAlignment = Alignment.Bottom,
//        horizontalArrangement = Arrangement.SpaceAround
//    ) {
//        data.forEach { (label, value) ->
//            val barHeight = (value / maxRevenue).toFloat() * 180f // tính tỉ lệ chiều cao
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Box(
//                    modifier = Modifier
//                        .width(24.dp)
//                        .height(barHeight.dp)
//                        .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(label, style = MaterialTheme.typography.labelSmall)
//            }
//        }
//    }
//}
//
//
//@Composable
//fun RecentTransactionItem(date: String, amount: String) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//    ) {
//        Text(
//            text = date,
//            modifier = Modifier.weight(1f),
//            style = MaterialTheme.typography.bodyMedium
//        )
//        Text(
//            text = amount,
//            style = MaterialTheme.typography.bodyMedium
//        )
//    }
//}
//
//fun calculateRevenueByFilter(orders: List<OrderModel>, filter: String): Double {
//    val currentDate = Calendar.getInstance()
//    return when (filter) {
//        "Ngày" -> {
//            // Tính doanh thu trong ngày
//            val dayStart = currentDate.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
//            val dayEnd = currentDate.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis
//            orders.filter { it.timestamp in dayStart..dayEnd }.sumOf { it.totalPrice }
//        }
//        "Tuần" -> {
//            // Tính doanh thu trong tuần
//            val weekStart = currentDate.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
//            val weekEnd = currentDate.apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis
//            orders.filter { it.timestamp in weekStart..weekEnd }.sumOf { it.totalPrice }
//        }
//        "Tháng" -> {
//            // Tính doanh thu trong tháng
//            val monthStart = currentDate.apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
//            val monthEnd = currentDate.apply { set(Calendar.DAY_OF_MONTH, currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis
//            orders.filter { it.timestamp in monthStart..monthEnd }.sumOf { it.totalPrice }
//        }
//        else -> 0.0
//    }
//}
//
//fun generateChartData(orders: List<OrderModel>, filter: String): List<Pair<String, Double>> {
//    val data = mutableListOf<Pair<String, Double>>()
//    val currentDate = Calendar.getInstance()
//
//    when (filter) {
//        "Ngày" -> {
//            // Tạo dữ liệu biểu đồ cho doanh thu theo ngày
//            val today = SimpleDateFormat("dd/MM", Locale.getDefault()).format(currentDate.time)
//            data.add(Pair(today, calculateRevenueByFilter(orders, "Ngày")))
//        }
//        "Tuần" -> {
//            // Tạo dữ liệu biểu đồ cho doanh thu theo tuần
//            val weekStart = currentDate.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }.timeInMillis
//            val weekEnd = currentDate.apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) }.timeInMillis
//            data.add(Pair("Tuần này", calculateRevenueByFilter(orders, "Tuần")))
//        }
//        "Tháng" -> {
//            // Tạo dữ liệu biểu đồ cho doanh thu theo tháng
//            val month = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(currentDate.time)
//            data.add(Pair(month, calculateRevenueByFilter(orders, "Tháng")))
//        }
//    }
//    return data
//}
//
//@Composable
//fun RevenueNavigation() {
//    val navController = rememberNavController()
//
//    NavHost(navController, startDestination = "overview") {
//        composable("overview") {
//            RevenueOverviewScreen(onChartClicked = {
//                navController.navigate("chart")
//            })
//        }
//
//        composable("chart") {
//            RevenueChartScreen()
//        }
//    }
//}
