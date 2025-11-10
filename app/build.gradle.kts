plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // The compose plugin is applied via the BOM now, so this alias is not always needed here
    // But it's good practice to declare what you use.
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp) // Use the alias for KSP
}

android {
    namespace = "com.stride.cashflow"
    // --- FIX 1: 'compileSdk' is a simple assignment ---
    compileSdk = 34 // Use a stable SDK version like 34

    defaultConfig {
        applicationId = "com.stride.cashflow"
        minSdk = 24
        targetSdk = 34 // Match compileSdk
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
        // --- FIX 2: Update Java version to match your JDK (21) ---
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        // --- FIX 3: Update Kotlin's JVM target ---
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // This is needed for Kotlin 2.0.0 and Compose
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- FIX 4: Use the version catalog for these dependencies too ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation("androidx.core:core-splashscreen:1.0.1")
}

