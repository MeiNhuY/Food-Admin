package com.example.adminapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.adminapp.Domain.OrderModel
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "notification") {
                composable("notification") {
                    NotificationScreen(navController = navController)
                }
                composable("orderDetailScreen/{orderId}") { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderDetailScreen(orderId = orderId) {
                        navController.popBackStack()
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current

    val db = FirebaseDatabase.getInstance().reference
    val currentUser = FirebaseAuth.getInstance().currentUser

    var orders by remember { mutableStateOf<List<OrderModel>>(emptyList()) }
    var role by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            db.child("Users").child(uid).child("role").get()
                .addOnSuccessListener { snapshot ->
                    role = snapshot.getValue(String::class.java) ?: ""

                    val orderRef = db.child("Order")
                    val listener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val orderList = mutableListOf<OrderModel>()
                            for (child in dataSnapshot.children) {
                                val order = child.getValue(OrderModel::class.java)
                                if (order != null) {
                                    if (role == "admin" || order.userId == uid) {
                                        orderList.add(order)
                                    }
                                }
                            }
                            orders = orderList.sortedByDescending { it.timestamp }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("NotificationScreen", "DB error: ${error.message}")
                        }
                    }

                    orderRef.addValueEventListener(listener)
                }
                .addOnFailureListener {
                    Log.e("NotificationScreen", "Failed to load role: ${it.message}")
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { innerPadding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Không có thông báo nào.")
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(orders) { order ->
                    NotificationCard(order = order, role = role) {
                        // Điều hướng khi nhấn nút "Xem chi tiết"
                        navController.navigate("orderDetailScreen/${order.orderId}")
                    }
                }
            }
        }
    }
}


@Composable
fun NotificationCard(order: OrderModel, role: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = R.drawable.thongbao),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp)
            )

            Column {
                Text(
                    text = if (role == "admin")
                        "Đơn hàng mới từ ${order.userName}"
                    else
                        "Bạn có 1 thông báo mới",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Đơn hàng #${order.orderId} hiện đang ở trạng thái: ${order.status}.",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (role != "admin") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hãy đánh giá sản phẩm trước ngày ${getReviewDeadline(order.timestamp)} để nhận 200 xu.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(order.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // Điều hướng khi nhấn vào nút
                Button(onClick = onClick) {
                    Text("Xem chi tiết")
                }
            }
        }
    }
}


//fun formatTimestamp(timestamp: Long): String {
//    val sdf = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())
//    return sdf.format(Date(timestamp))
//}

fun getReviewDeadline(timestamp: Long): String {
    val deadline = Calendar.getInstance().apply {
        timeInMillis = timestamp
        add(Calendar.DAY_OF_MONTH, 30)
    }
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(deadline.time)
}
