package com.example.adminapp.Domain

data class UserModel(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = ""
){}