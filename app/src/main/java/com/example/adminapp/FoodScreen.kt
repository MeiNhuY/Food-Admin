package com.example.adminapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.adminapp.Domain.CategoryModel
import com.example.adminapp.Domain.FoodModel
import com.example.adminapp.MainViewModel.MainViewModel


@Composable
fun FoodScreen(navController: NavHostController) {
    val mainRepository = remember { MainRepository() }
    val context = LocalContext.current
    val categories = remember { mutableStateOf<List<CategoryModel>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }
    var allItems by remember { mutableStateOf<List<FoodModel>>(emptyList()) }

    var confirmDelete by remember { mutableStateOf<Pair<Boolean, String>>(false to "") }  // Trạng thái xác nhận xóa

    // Lắng nghe sự thay đổi reloadFoodList
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val reloadFoodList = currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("reloadFoodList")
        ?.observeAsState()

    // Load food items
    LaunchedEffect(true) {
        mainRepository.getFoodsFromFirebase(
            onSuccess = { foodList -> allItems = foodList },
            onFailure = { e -> println("Failed to load foods: ${e.message}") }
        )
    }

    // Load categories
    LaunchedEffect(true) {
        mainRepository.getCategoriesFromFirebase(
            onSuccess = { categoriesList ->
                val allCategory = CategoryModel(Id = 0, Name = "All", ImagePath = "")
                categories.value = listOf(allCategory) + categoriesList
            },
            onFailure = { exception -> println("Error getting categories: ${exception.message}") }
        )
    }

    // Lắng nghe reload yêu cầu để reload lại danh sách
    LaunchedEffect(reloadFoodList?.value) {
        if (reloadFoodList?.value == true) {
            mainRepository.getFoodsFromFirebase(
                onSuccess = { foodList -> allItems = foodList },
                onFailure = { e -> println("Failed to reload foods: ${e.message}") }
            )
            currentBackStackEntry?.savedStateHandle?.set("reloadFoodList", false)
        }
    }

    val filteredItems = if (selectedCategoryId == "All") allItems
    else allItems.filter { it.CategoryId == selectedCategoryId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFFDFDFD))
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Food Menu",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = Color.Black
            )

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.value.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.Name) },
                            onClick = {
                                selectedCategoryId = if (cat.Name == "All") "All" else cat.Id.toString()
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Food list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredItems) { item ->
                FoodCard(
                    item = item,
                    onClick = { navController.navigate("detailFoodScreen/${item.Id}") },
                    onEdit = { navController.navigate("detailFoodScreen/${item.Id}") },
                    onDelete = {
                        confirmDelete = true to item.Id.toString() // Hiển thị dialog xác nhận xóa
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("addFoodScreen") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC5835)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Add Food", color = Color.White)
        }
    }

    // Dialog xác nhận xóa
    if (confirmDelete.first) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false to "" },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn chắc chắn muốn xóa món ăn này?") },
            confirmButton = {
                TextButton(onClick = {
                    // Xóa món ăn
                    mainRepository.deleteFoodById(context, confirmDelete.second) {
                        // Reload danh sách món ăn sau khi xóa
                        mainRepository.getFoodsFromFirebase(
                            onSuccess = { foodList -> allItems = foodList },
                            onFailure = { e -> println("Failed to reload foods: ${e.message}") }
                        )
                    }
                    confirmDelete = false to ""  // Đóng dialog
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false to "" }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun FoodCard(item: FoodModel, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF2F2F2))
            .padding(10.dp)
            .clickable { onClick() }
    ) {
        val painter = rememberAsyncImagePainter(item.ImagePath ?: "")

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.Title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${item.TimeValue} min", fontSize = 12.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(item.Star.toString(), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("$${item.Price}", fontWeight = FontWeight.Bold)
        }

        IconButton(onClick = { onEdit() }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }

        IconButton(onClick = { onDelete() }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewFoodScreen() {
    val navController = rememberNavController()
    FoodScreen(navController)
}