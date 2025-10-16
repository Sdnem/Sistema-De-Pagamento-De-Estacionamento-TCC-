plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // ===============================================
    // 1. ADICIONE O NOVO PLUGIN DO COMPILADOR COMPOSE AQUI
    id("org.jetbrains.kotlin.plugin.compose")
    // ===============================================
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    // ===============================================
    // 2. REMOVA ESTE BLOCO COMPLETAMENTE
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.1"
    // }
    // ===============================================
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Suas dependências continuam as mesmas
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation(libs.play.services.cast.framework)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation(libs.androidx.lifecycle.runtime.compose)
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    testImplementation(libs.junit)
    implementation("androidx.compose.material:material-icons-extended")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}

