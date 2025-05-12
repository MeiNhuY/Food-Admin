package com.example.adminapp

import android.util.Log
import androidx.compose.foundation.clickable
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
import com.example.adminapp.Domain.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBackClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {}
) {
        val orders = remember { mutableStateOf<List<OrderModel>>(emptyList()) }
        val isAdmin = remember { mutableStateOf(false) }
        val repository = MainRepository() // Create instance of the repository
        val context = LocalContext.current // Get the context

        // Kiểm tra vai trò người dùng (admin hay không)
        LaunchedEffect(Unit) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            ref.child("role").get().addOnSuccessListener {
                val role = it.value as? String
                Log.d("UserRole", "Fetched role: $role")
                isAdmin.value = role == "admin"
            }
        }

        // Lấy danh sách đơn hàng nếu người dùng là admin
        LaunchedEffect(isAdmin.value) {
            if (isAdmin.value) {
                // Fetch orders from the database and update the state
                repository.getOrders(context = context, callback = { fetchedOrders ->
                    orders.value = fetchedOrders
                })
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Đơn Hàng") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                )
            }
        ) { padding ->
            if (isAdmin.value) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (orders.value.isEmpty()) {
                        item {
                            Text(
                                text = "Không có đơn hàng nào.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        items(orders.value) { order ->
                            OrderCard(order = order, onClick = { onOrderClick(order.orderId) })
                        }
                    }
                }
            } else {
                // Hiển thị thông báo nếu người dùng không phải admin
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bạn không có quyền truy cập vào danh sách đơn hàng.")
                }
            }
        }
    }

@Composable
fun OrderCard(order: OrderModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mã đơn: ${order.orderId}", fontWeight = FontWeight.Bold)
            Text("Khách hàng: ${order.userName}")
            Text("Tổng tiền: ${order.totalPrice} VND")
            Text("Trạng thái: ${order.status}")
        }
    }
}
