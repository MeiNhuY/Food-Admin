package com.example.adminapp

import android.Manifest
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.adminapp.Domain.CategoryModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminapp.Domain.FoodModel
import com.example.adminapp.MainViewModel.MainViewModel


@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable


fun AddFoodScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val mainRepository = remember { MainRepository() }

    // Các trạng thái
    var selectedCategory by remember { mutableStateOf("Select Category") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemTime by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val categories = remember { mutableStateOf<List<CategoryModel>>(emptyList()) }

    // Request permission
    RequestStoragePermission()

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    // Load categories
    LaunchedEffect(true) {
        mainRepository.getCategoriesFromFirebase(
            onSuccess = { categoryList -> categories.value = categoryList },
            onFailure = { e -> println("Load categories error: ${e.message}") }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Add Food",
                fontSize = 24.sp,
                modifier = Modifier.weight(1f),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown category
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.value.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.Name) },
                        onClick = {
                            selectedCategory = category.Name
                            selectedCategoryId = category.Id.toString()
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = itemPrice,
            onValueChange = { itemPrice = it },
            label = { Text("Item Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = itemTime,
            onValueChange = { itemTime = it },
            label = { Text("Time (min)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Image Picker
        Text("Item Image", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            if (imageUri != null) {
                val bitmap = remember(imageUri) {
                    try {
                        if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, imageUri!!)
                            ImageDecoder.decodeBitmap(source)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Pick Image", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = itemDescription,
            onValueChange = { itemDescription = it },
            placeholder = { Text("Write something...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.weight(1f))

        // Submit
        Button(
            onClick = {
                if (
                    itemName.isBlank() || itemPrice.isBlank() || itemTime.isBlank() ||
                    itemDescription.isBlank() || selectedCategoryId.isBlank() || imageUri == null
                ) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val foodId = (100000..999999).random() // Random int ID
                    val food = FoodModel(
                        Id = foodId,
                        Title = itemName,
                        Price = itemPrice.toDoubleOrNull() ?: 0.0,
                        TimeValue = itemTime.toIntOrNull() ?: 0,
                        Description = itemDescription,
                        CategoryId = selectedCategoryId,
                        ImagePath = "" // Will be updated after upload
                    )

                    mainRepository.uploadFoodWithImageToCloudinary(
                        food = food,
                        imageUri = imageUri!!,
                        context = context,
                        onSuccess = {
                            Toast.makeText(context, "Food added!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        onFailure = {
                            Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC5835)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add", color = Color.White)
        }
    }
}




@RequiresApi(Build.VERSION_CODES.P)
@Preview(showBackground = true)
@Composable
fun PreviewAddFoodScreen() {
    AddFoodScreen(onBack = {})
}
