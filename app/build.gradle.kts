plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.almeria53.store"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.almeria53.store"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }

    // Firma FIJA: siempre la misma llave, para que cada versión nueva
    // se instale encima de la anterior sin desinstalar nada.
    signingConfigs {
        create("fija") {
            storeFile = file("apps53.keystore")
            storePassword = "apps53pass"
            keyAlias = "apps53"
            keyPassword = "apps53pass"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("fija")
        }
        release {
            signingConfig = signingConfigs.getByName("fija")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("io.coil-kt:coil-compose:2.6.0")
}
