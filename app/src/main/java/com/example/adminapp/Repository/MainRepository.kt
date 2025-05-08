package com.example.adminapp

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.adminapp.Domain.CategoryModel
import com.example.adminapp.Domain.FoodModel
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Log hoặc xử lý lỗi
            }
        })
        return listData
    }

    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>> {
        val listData = MutableLiveData<MutableList<FoodModel>>()
        val ref = firebaseDatabase.getReference("Foods")
        val query = ref.orderByChild("CategoryId").equalTo(id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<FoodModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(FoodModel::class.java)
                    list?.let { lists.add(it) }
                }
                listData.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                // Log hoặc xử lý lỗi
            }
        })
        return listData
    }

    fun uploadImageOnlyToCloudinary(
        imageUri: Uri,
        context: Context,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val filePath = getRealPathFromURI(context, imageUri)
        if (filePath.isNullOrEmpty()) {
            onFailure(Exception("File path is null or empty"))
            return
        }

        val file = File(filePath)
        if (!file.exists()) {
            onFailure(Exception("File does not exist"))
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", "adminpic")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/mainhuy/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val imageUrl = JSONObject(responseBody ?: "").getString("secure_url")
                    onSuccess(imageUrl)
                } else {
                    onFailure(Exception("Upload failed: ${response.message}"))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onFailure(e)
            }
        })
    }


    //CATEGORY
    //Delete Category
    fun deleteCategory(categoryId: Int) {
        val ref = firebaseDatabase.getReference("Category")
        ref.child(categoryId.toString()).removeValue()
    }

    //Add Category
    fun uploadCategoryWithImageToCloudinary(
        Id: String,
        Name: String,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val filePath = getRealPathFromURI(context, imageUri)
        if (filePath.isNullOrEmpty()) {
            Log.e("UploadError", "Cannot read the image file")
            showToastOnMainThread(context, "Không thể đọc ảnh")
            onFailure(Exception("File path is null or empty"))
            return
        }

        val file = File(filePath)
        if (!file.exists()) {
            Log.e("UploadError", "File does not exist")
            showToastOnMainThread(context, "File không tồn tại")
            onFailure(Exception("File does not exist"))
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", "adminpic") // đảm bảo "ml_default" là unsigned preset
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/mainhuy/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("Cloudinary", "Upload successful. Response: $responseBody")

                    if (responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val imageUrl = jsonResponse.getString("secure_url")

                        val category = CategoryModel(
                            Id = Id.toInt(),
                            Name = Name,
                            ImagePath = imageUrl
                        )

                        //luu xuong Fbase
                        firebaseDatabase.getReference("Category")
                            .child(Id)
                            .setValue(category)
                            .addOnSuccessListener {
                                Log.d("UploadSuccess", "Category added successfully")
                                showToastOnMainThread(context, "Category added")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e("UploadError", "Failed to add category: ${e.message}")
                                showToastOnMainThread(
                                    context,
                                    "Failed to add category: ${e.message}"
                                )
                                onFailure(e)
                            }
                    } else {
                        Log.e("UploadError", "Response body is null")
                        showToastOnMainThread(context, "Upload failed: No response body")
                        onFailure(Exception("No response body from Cloudinary"))
                    }
                } else {
                    val errorBody = response.body?.string()
                    Log.e("UploadError", "Upload failed: ${response.code} - ${response.message}")
                    Log.e("UploadError", "Error body: $errorBody")
                    showToastOnMainThread(context, "Upload thất bại: ${response.message}")
                    onFailure(Exception("Upload failed with code ${response.code}"))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadError", "Image upload failed: ${e.message}")
                showToastOnMainThread(context, "Image upload failed: ${e.message}")
                onFailure(e)
            }
        })
    }

    private fun showToastOnMainThread(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            val name = it.getString(nameIndex)

            val file = File(context.cacheDir, name)
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            return file.absolutePath
        }
        return null
    }
    //end - add category


    //Edit Category
    fun editCategoryWithImage(
        category: CategoryModel,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (imageUri == null) {
            // Nếu không có ảnh mới, chỉ cập nhật thông tin danh mục
            firebaseDatabase.getReference("Category")
                .child(category.Id.toString())
                .setValue(category)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        } else {
            // Nếu có ảnh mới, upload ảnh và cập nhật thông tin danh mục với ảnh mới
            uploadImageOnlyToCloudinary(
                imageUri = imageUri,
                context = context,
                onSuccess = { imageUrl ->
                    // Tạo lại category với imageUrl mới
                    val updatedCategory = category.copy(ImagePath = imageUrl)

                    // Lưu category đã cập nhật vào Firebase
                    firebaseDatabase.getReference("Category")
                        .child(category.Id.toString())
                        .setValue(updatedCategory)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                },
                onFailure = { e ->
                    onFailure(e) // Xử lý lỗi upload ảnh
                }
            )
        }
    }




    //FOOD
    fun getCategoriesFromFirebase(
        onSuccess: (List<CategoryModel>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance().getReference("Category")
        database.get().addOnSuccessListener { snapshot ->
            val categories = mutableListOf<CategoryModel>()
            snapshot.children.forEach { categorySnapshot ->
                val category = categorySnapshot.getValue(CategoryModel::class.java)
                if (category != null) {
                    categories.add(category)
                }
            }
            onSuccess(categories)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }



    //Lấy Foods hiển thị ra giao diện
    fun getFoodsFromFirebase(
        onSuccess: (List<FoodModel>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val databaseRef = firebaseDatabase.getReference("Foods")
        databaseRef.get()
            .addOnSuccessListener { snapshot ->
                val foodList = mutableListOf<FoodModel>()
                for (foodSnapshot in snapshot.children) {
                    val food = foodSnapshot.getValue(FoodModel::class.java)
                    food?.let { foodList.add(it) }
                }
                onSuccess(foodList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Phương thức lấy món ăn theo ID
    fun getFoodByIdFromFirebase(foodId: Int, onSuccess: (FoodModel?) -> Unit, onFailure: (Exception) -> Unit) {
        val ref = firebaseDatabase.getReference("Foods").child(foodId.toString())
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val food = snapshot.getValue(FoodModel::class.java)
                onSuccess(food)
            } else {
                onSuccess(null) // Không tìm thấy món ăn
            }
        }.addOnFailureListener { exception ->
            onFailure(exception) // Xử lý lỗi nếu có
            Log.e("GetFoodById", "Error fetching food by ID: ${exception.message}")
        }
    }


    //Delete food
    fun deleteFoodById(context: Context, foodId: String, onComplete: () -> Unit) {
        val ref = firebaseDatabase.getReference("Foods")
        ref.child(foodId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "✅ Food deleted successfully.", Toast.LENGTH_SHORT).show()
                Log.d("DeleteFood", "Deleted food with id: $foodId")
                onComplete()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "❌ Failed to delete food: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DeleteFood", "Error deleting food: ${e.message}")
            }
    }


    //Add Food
    fun uploadFoodWithImageToCloudinary(
        food: FoodModel,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val filePath = getRealPathFromURI(context, imageUri)
        if (filePath.isNullOrEmpty()) {
            onFailure(Exception("File path is null or empty"))
            return
        }

        val file = File(filePath)
        if (!file.exists()) {
            onFailure(Exception("File does not exist"))
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", "adminpic")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/mainhuy/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val imageUrl = JSONObject(responseBody ?: "").getString("secure_url")
                    val foodWithImage = food.copy(ImagePath = imageUrl)

                    // Save food to Firebase
                    FirebaseDatabase.getInstance().getReference("Foods")
                        .child(food.Id.toString())
                        .setValue(foodWithImage)
                        .addOnSuccessListener {
                            showToastOnMainThread(context, "Food added successfully")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            showToastOnMainThread(context, "Failed to add food: ${e.message}")
                            onFailure(e)
                        }
                } else {
                    onFailure(Exception("Upload failed: ${response.message}"))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onFailure(e)
            }
        })
    }


    //Edit food
    fun editFoodWithImage(
        food: FoodModel,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (imageUri == null) {
            // Nếu không có ảnh mới, chỉ cập nhật thông tin món ăn
            firebaseDatabase.getReference("Foods")
                .child(food.Id.toString())
                .setValue(food)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        } else {
            // Nếu có ảnh mới, upload ảnh và cập nhật thông tin món ăn với ảnh mới
            uploadImageOnlyToCloudinary(
                imageUri = imageUri,
                context = context,
                onSuccess = { imageUrl ->
                    val updatedFood = food.copy(ImagePath = imageUrl)
                    firebaseDatabase.getReference("Foods")
                        .child(food.Id.toString())
                        .setValue(updatedFood)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                },
                onFailure = { e -> onFailure(e) }
            )
        }
    }




}
