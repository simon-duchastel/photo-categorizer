plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.duchastel.simon.photocategorizer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.duchastel.simon.photocategorizer"
        minSdk = 26
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
        buildConfig = true
        compose = true
    }
}

dependencies {
    // Business logic modules
    implementation(project(":modules:lib:auth"))
    implementation(project(":modules:lib:filemanager"))
    implementation(project(":modules:lib:storage"))
    implementation(project(":modules:lib:dropbox"))
    implementation(project(":modules:lib:navigation"))
    implementation(project(":modules:lib:utils"))
    
    // UI modules
    implementation(project(":modules:ui:theme"))
    implementation(project(":modules:ui:components"))
    implementation(project(":modules:ui:screens:login"))
    implementation(project(":modules:ui:screens:photoswiper"))
    implementation(project(":modules:ui:screens:settings"))
    implementation(project(":modules:ui:screens:splash"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    ksp(libs.hilt.compiler)
    implementation(libs.hilt)
    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
