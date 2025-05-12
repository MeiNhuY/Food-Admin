package com.example.adminapp.MainViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.adminapp.Domain.OrderModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State


class DashboardViewModel : ViewModel() {

    private val _totalOrders = mutableStateOf(0)
    val totalOrders: State<Int> = _totalOrders

    private val _totalCustomers = mutableStateOf(0)
    val totalCustomers: State<Int> = _totalCustomers

    private val _totalRevenue = mutableStateOf(0.0)
    val totalRevenue: State<Double> = _totalRevenue

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        Firebase.firestore.collection("Order")
            .get()
            .addOnSuccessListener { result ->
                _totalOrders.value = result.size()
                var revenue = 0.0
                result.forEach { doc ->
                    val order = doc.toObject(OrderModel::class.java)
                    revenue += order.totalPrice
                }
                _totalRevenue.value = revenue
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching orders: $exception")
            }


        Firebase.firestore.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                _totalCustomers.value = result.size()
            }
    }
}
