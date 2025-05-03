plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Utiliser l'ID direct sans alias pour éviter le conflit de version
}

android {
    namespace = "fr.bdst.aatt"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.bdst.aatt"
        minSdk = 29
        targetSdk = 35
        versionCode = 3
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Configuration pour la signature de l'application
    signingConfigs {
        create("release") {
            storeFile = file("../aatt.keystore")
            storePassword = "AAtt2142"
            keyAlias = "aatt"
            keyPassword = "AAtt2142"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Activer la minification pour réduire la taille de l'APK
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release") // Utiliser la config de signature
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
    // Base Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.appcompat:appcompat:1.6.1") // Ajout pour SplashActivity
    
    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // DocumentFile pour le Storage Access Framework (SAF)
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // ViewModel & LiveData
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    
    // JSON Serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}