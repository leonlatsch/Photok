val telemetryDeckAppId: String? by project

plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
}

android {
    namespace = "dev.leonlatsch.photok.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 28

        buildConfigField(
            "String",
            "TELEMETRY_DECK_APP_ID",
            "\"${telemetryDeckAppId.orEmpty()}\""
        )
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            isDefault = true
            dimension = "distribution"
        }
        create("foss") {
            dimension = "distribution"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    // Room
    api("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-android-compiler:2.60.1")

    // Gson (for Converters)
    implementation("com.google.code.gson:gson:2.14.0")

    // Telemetry (TelemetryService lives here so :app and :pro can both signal)
    implementation("com.telemetrydeck:kotlin-sdk:7.2.0")

    // Timber (for VaultFileStorage)
    implementation("com.jakewharton.timber:timber:5.0.1")

    // SharedPreferences edit extension
    implementation("androidx.core:core-ktx:1.19.0")

    // AppCompat (drawables in res/drawable reference ?attr/colorControlNormal)
    implementation("androidx.appcompat:appcompat:1.8.0")

    // Timber Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Compose (AppTheme lives here so it can be shared across modules for previews)
    api(platform("androidx.compose:compose-bom:2026.09.00"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.material3:material3")
    api("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
