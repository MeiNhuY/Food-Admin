package com.example.adminapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.adminapp.Domain.CategoryModel
import com.example.adminapp.MainViewModel.MainViewModel
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage


@Composable
fun EditCategoryScreen(
    navController: NavController,
    category: CategoryModel,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    var categoryName by remember { mutableStateOf(category.Name) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = category.Id.toString(),
            onValueChange = {},
            label = { Text("Category ID") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("Category Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Choose New Image (Optional)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        imageUri?.let { uri ->
            val bitmap = remember(uri) {
                try {
                    if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
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
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        } ?: run {
            Text("Current Image:")
            AsyncImage(
                model = category.ImagePath,
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (categoryName.isBlank()) {
                    Toast.makeText(context, "Please enter category name", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedCategory = category.copy(Name = categoryName)
                    viewModel.editCategory(
                        category = updatedCategory,
                        imageUri = imageUri,
                        context = context
                    ) {
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC5835)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Changes", color = Color.White)
        }
    }
}
