package com.example.adminapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.adminapp.Domain.CategoryModel
import com.example.adminapp.MainViewModel.MainViewModel
import coil.compose.AsyncImage


@Composable
fun CategoryScreen(navController: NavHostController, viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val categoryList by viewModel.loadCategory().observeAsState(initial = mutableListOf())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(
            text = "All Category",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categoryList) { category ->
                CategoryModelCard(
                    category = category,
                    onDelete = { viewModel.deleteCategory(category.Id) },
                    navController = navController // TRUYỀN Ở ĐÂY
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("addCategoryScreen")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC5835)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Add Category", color = Color.Black)
        }
    }
}


@Composable
fun CategoryModelCard(category: CategoryModel, onDelete: () -> Unit, navController: NavHostController) {
    var confirmDelete by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD5D4D4))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = category.ImagePath,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(25.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(category.Name, fontWeight = FontWeight.Bold)
            Text("ID: ${category.Id}", color = Color.Gray, fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = {
                navController.navigate("editCategoryScreen/${category.Id}")
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }

            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }

    // AlertDialog xác nhận xóa
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn chắc chắn muốn xóa danh mục này?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete() // Gọi hàm xóa khi xác nhận
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewCategoryScreen() {
    val navController = rememberNavController()
    CategoryScreen(navController)
}

