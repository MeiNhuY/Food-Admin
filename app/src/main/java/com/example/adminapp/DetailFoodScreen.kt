package com.example.adminapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.adminapp.Domain.FoodModel
import com.example.adminapp.MainViewModel.MainViewModel
import org.tensorflow.lite.support.label.Category


@Composable
fun DetailFoodScreen(navController: NavHostController, foodId: String) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val repository = remember { MainRepository() }

    var foodItem by remember { mutableStateOf<FoodModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var confirmDelete by remember { mutableStateOf(false) }

    // State gốc
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    // URI ảnh gốc từ firebase
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // URI ảnh được chọn mới
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var isEditing by remember { mutableStateOf(false) }

    // Launcher chọn ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LaunchedEffect(foodId) {
        repository.getFoodByIdFromFirebase(foodId.toInt(), {
            foodItem = it
            it?.let {
                title = it.Title ?: ""
                price = it.Price.toString()
                description = it.Description ?: ""
                time = it.TimeValue.toString()
                imageUri = it.ImagePath?.let { Uri.parse(it) }
            }
            isLoading = false
        }, {
            isLoading = false
        })
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (foodItem == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không thể tải dữ liệu.")
        }
        return
    }

    val item = foodItem!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "Detail Food",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        color = Color.Black
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item Name", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp) },
                    trailingIcon = { if (isEditing) Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Item Price", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp) },
                    trailingIcon = { if (isEditing) Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Item Time", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp) },
                    trailingIcon = { if (isEditing) Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Item Image", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Ưu tiên ảnh được chọn mới, nếu có
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri ?: imageUri ?: ""),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nút chọn ảnh mới
                if (isEditing) {
                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Chọn ảnh mới")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Item Description", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp) },
                    trailingIcon = { if (isEditing) Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (isEditing) {
                            val updatedFood = foodItem!!.copy(
                                Title = title,
                                Price = price.toDoubleOrNull() ?: 0.0,
                                Description = description,
                                TimeValue = time.toIntOrNull() ?: 0
                            )
                            viewModel.editFood(
                                food = updatedFood,
                                imageUri = selectedImageUri, // chỉ gửi ảnh nếu có chọn mới
                                context = context,
                                onSuccess = {

                                    // Notify reload data in FoodScreen
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("reloadFoodList", true)

                                    isEditing = false
                                    foodItem = updatedFood
                                    // Nếu có ảnh mới, cập nhật lại hiển thị
                                    if (selectedImageUri != null) {
                                        imageUri = selectedImageUri
                                        selectedImageUri = null
                                    }
                                }
                            )
                        } else {
                            isEditing = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE3B0E)),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(if (isEditing) "Save" else "Edit")
                }


                Button(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D0909)),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("Delete")
                }
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn chắc chắn muốn xóa món ăn này?") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDelete = false
                        repository.deleteFoodById(context, foodId) {
                            navController.popBackStack()
                        }
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
}


@Preview(showBackground = true)
@Composable
private fun PreviewDetailFoodScreen() {
    val navController = rememberNavController()
    DetailFoodScreen(navController = navController, foodId = "1")
}
