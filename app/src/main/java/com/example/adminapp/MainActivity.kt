package com.example.adminapp



import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adminapp.MainViewModel.MainViewModel
import com.example.adminapp.ui.theme.AdminAppTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.google.firebase.Firebase
import com.google.firebase.initialize


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminAppTheme {
                AppNavigation()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AppNavigation() {
    class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            Firebase.initialize(this) // Initialize Firebase
        }
    }

    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("login") { inclusive = true } // Xóa màn hình login khỏi back stack
                    }
                }
            )
        }
        composable("admin_dashboard") {
            AdminDashboardScreen(
                pendingOrders = 12,
                completedOrders = 34,
                totalEarnings = "$560",
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        //Category
        composable("category") {
            CategoryScreen(navController)
        }

        composable("addCategoryScreen") {
            AddCategoryScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable("editCategoryScreen/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull()
            categoryId?.let { id ->
                val categoryData = viewModel.getCategoryById(id)
                if (categoryData != null) {
                    EditCategoryScreen(navController, categoryData)
                } else {
                    // Không tìm thấy category, quay lại màn hình trước
                    navController.popBackStack()
                }
            } ?: navController.popBackStack() // categoryId là null hoặc lỗi parse
        }




        //Food
        composable("food") {
            FoodScreen(navController)
        }

        composable("addFoodScreen") {
            AddFoodScreen(onBack = { navController.popBackStack() })
        }

        composable("detailFoodScreen/{foodId}") { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            DetailFoodScreen(navController = navController, foodId = foodId)
        }
        // Order list screen
        composable("orders") {
            OrdersScreen(
                onBackClick = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate("orderDetailScreen/$orderId")
                }
            )
        }
        // Order detail screen with parameter
        composable("orderDetailScreen/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(orderId = orderId, onBack = { navController.popBackStack() })
        }


        //Revenue
        composable("revenue") {
            RevenueScreen()
        }

        //Profile
        composable("profile") {
            AdminProfileScreen() // Thêm màn hình profile
        }

    }
}






