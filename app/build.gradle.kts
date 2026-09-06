plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

gradle.taskGraph.whenReady {
    val signingTasks = setOf("assembleRelease", "bundleRelease", "packageRelease")
    if (allTasks.any { it.name in signingTasks } &&
        System.getenv("RELEASE_KEYSTORE_PATH").isNullOrBlank()) {
        throw GradleException(
            "RELEASE_KEYSTORE_PATH not set - refusing to build an unsigned release APK."
        )
    }
}

android {
    namespace = "com.ksiig.tmuxmobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ksiig.tmuxmobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "0.4.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // DEV TRACK - existing, unchanged. Do NOT delete.
        val debugKeystorePath = System.getenv("DEBUG_KEYSTORE_PATH")
        if (debugKeystorePath != null) {
            named("debug") {
                storeFile = file(debugKeystorePath)
                storePassword = System.getenv("DEBUG_KEYSTORE_PASSWORD") ?: "android"
                keyAlias = "tmux-mobile"
                keyPassword = System.getenv("DEBUG_KEYSTORE_PASSWORD") ?: "android"
            }
        }

        // PROD TRACK - new.
        create("release") {
            val path = System.getenv("RELEASE_KEYSTORE_PATH")
            if (!path.isNullOrBlank()) {
                storeFile = file(path)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true                                  // keep
            isShrinkResources = true                                // keep
            proguardFiles(                                          // keep
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")     // add
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
