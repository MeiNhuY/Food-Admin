package com.example.adminapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.getValue
import com.example.adminapp.MainViewModel.DashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChartScreen(viewModel: DashboardViewModel = viewModel(),onBack: () -> Unit) {
    val orders by viewModel.totalOrders
    val customers by viewModel.totalCustomers
    val revenue by viewModel.totalRevenue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp)
    ) {
        TopBar()
        Spacer(modifier = Modifier.height(16.dp))
        SummaryCards(orders = orders, customers = customers)
        Spacer(modifier = Modifier.height(16.dp))
        RevenueChartCard(revenue = revenue)
    }
}


@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("Brian Lee", fontWeight = FontWeight.Bold)
                Text("Admin", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        }
    }
}

@Composable
fun SummaryCards(orders: Int, customers: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(orders.toString(), "Total Orders", Modifier.weight(1f))
        SummaryCard(customers.toString(), "Total Customers", Modifier.weight(1f))
    }
}

@Composable
fun SummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA07A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = label, color = Color.White)
        }
    }
}


@Composable
fun RevenueChartCard(revenue: Double) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Revenue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Lorem ipsum dolor sit amet", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Income", style = MaterialTheme.typography.bodySmall)
            Text("$${String.format("%,.0f", revenue)}", fontWeight = FontWeight.Bold, fontSize = 22.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, shape = RoundedCornerShape(50)) { Text("All Food") }
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(50)) { Text("Food") }
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(50)) { Text("Beverages") }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFFFE4E1), RoundedCornerShape(16.dp))
            ) {
                Text("Line Chart", modifier = Modifier.align(Alignment.Center), color = Color.DarkGray)
            }
        }
    }
}

