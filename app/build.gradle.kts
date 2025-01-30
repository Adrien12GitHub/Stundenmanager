plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.stundenmanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stundenmanager"
        minSdk = 24
        targetSdk = 34
        //versionCode = 1
        //versionName = "1.0" // Manual versioning

        // Read versionCode from GitHub Actions ENV, if available, otherwise from file
        val versionCodeEnv = System.getenv("VERSION_CODE")?.toIntOrNull()
        val versionCodeFile = file("version.txt")
        val versionCode = versionCodeEnv ?: versionCodeFile.readText().trim().toInt()

        this.versionCode = versionCode

        // Generate version name automatically from versionCode
        val versionNameEnv = System.getenv("VERSION_NAME")
        val versionName = versionNameEnv ?: "0.$versionCode"
        this.versionName = versionName

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation (libs.firebase.auth.v2106)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.itextpdf:itext7-core:7.1.13")
}