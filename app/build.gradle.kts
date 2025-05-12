plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.adminapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.adminapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.volley)
    implementation(libs.androidx.espresso.core)
    implementation(libs.play.services.base)
    implementation(libs.play.services.base)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.litert.support.api)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //
    implementation("io.coil-kt:coil-compose:2.2.2") // Check for the latest version // Coil (nếu dùng ảnh từ URL)
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")
    implementation("com.google.accompanist:accompanist-pager:0.28.0") // Check for the latest version
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.5")
    implementation("com.github.bumptech.glide:glide:4.13.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    implementation("androidx.compose.foundation:foundation:1.7.6")

    implementation("androidx.compose.material3:material3:1.0.0-alpha13")
    implementation("androidx.compose.material:material-icons-extended")

    //thu vien trươt cho banner

    implementation("androidx.navigation:navigation-compose:2.7.7") // bản mới nhất navigation

    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")

    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    implementation("androidx.activity:activity-compose:1.7.2")



}