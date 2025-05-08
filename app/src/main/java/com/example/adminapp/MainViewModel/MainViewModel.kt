package com.example.adminapp.MainViewModel

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.adminapp.Domain.CategoryModel
import com.example.adminapp.Domain.FoodModel
import com.example.adminapp.MainRepository
import androidx.compose.runtime.State



class MainViewModel : ViewModel() {
    private val repository = MainRepository()

    // Sửa đúng tại đây
    private val _categoryList: LiveData<MutableList<CategoryModel>> = repository.loadCategory()
    val categoryList: LiveData<MutableList<CategoryModel>> get() = _categoryList

    private val _foodList = mutableStateOf<List<FoodModel>>(emptyList())
    val foodList: State<List<FoodModel>> get() = _foodList


    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        return _categoryList
    }

    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>> {
        return repository.loadFiltered(id)
    }

    fun getAllCategories(): LiveData<MutableList<CategoryModel>> {
        return _categoryList
    }

    fun getCategoryById(categoryId: Int): CategoryModel? {
        return _categoryList.value?.firstOrNull { it.Id == categoryId }
    }

    fun deleteCategory(categoryId: Int) {
        repository.deleteCategory(categoryId)
    }

    fun uploadCategoryWithImage(
        id: String,
        name: String,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit
    ) {
        repository.uploadCategoryWithImageToCloudinary(
            Id = id,
            Name = name,
            imageUri = imageUri,
            context = context,
            onSuccess = onSuccess,
            onFailure = { e ->
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun editCategory(
        category: CategoryModel,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        repository.editCategoryWithImage(
            category = category,
            imageUri = imageUri,
            context = context,
            onSuccess = onSuccess,
            onFailure = {
                Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }



    //Food
    //add Food
    fun uploadFoodWithImage(
        food: FoodModel,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit
    ) {
        repository.uploadFoodWithImageToCloudinary(
            food = food,
            imageUri = imageUri,
            context = context,
            onSuccess = onSuccess,
            onFailure = { e ->
                // Đảm bảo Toast chạy trên UI thread
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Lỗi khi thêm món ăn: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


    fun setFoodList(foodList: List<FoodModel>) {
        _foodList.value = foodList
    }


    fun getFoodById(id: String): FoodModel? {
        return _foodList.value.find { it.Id.toString() == id }
    }

    fun editFood(
        food: FoodModel,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        repository.editFoodWithImage(
            food = food,
            imageUri = imageUri,
            context = context,
            onSuccess = {
                Toast.makeText(context, "Đã sửa thông tin thành công", Toast.LENGTH_SHORT).show()
                onSuccess()
            },
            onFailure = {
                Log.e("editFood", "Lỗi khi lưu: ${it.message}", it)
                Toast.makeText(context, "Lỗi khi lưu: ${it.message}", Toast.LENGTH_LONG).show()
            }
        )
    }





}

