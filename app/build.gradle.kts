import com.android.sdklib.AndroidVersion.VersionCodes

plugins {
    id("com.android.application")
    id("com.jaredsburrows.license")
    kotlin("android")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
}

val appVersionName: String by project
val appVersionCode: String by project

apply(plugin = "androidx.navigation.safeargs.kotlin")
apply(plugin = "dagger.hilt.android.plugin")

android {
    compileSdk = VersionCodes.VANILLA_ICE_CREAM

    defaultConfig {
        applicationId = "dev.leonlatsch.photok"
        minSdk = VersionCodes.P
        targetSdk = VersionCodes.VANILLA_ICE_CREAM

        versionCode = appVersionCode.toInt()
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += "room.incremental" to "true"
                arguments += "room.schemaLocation" to "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        dataBinding = true
        compose = true
        buildConfig = true
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lint {
        lintConfig = file("$rootDir/gradle/lint.xml")
    }
    namespace = "dev.leonlatsch.photok"
}

licenseReport {
    copyCsvReportToAssets = false
    copyHtmlReportToAssets = true
    copyJsonReportToAssets = false
}

dependencies {
    // Architectural Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-paging:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.7.2")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Coroutine Lifecycle Scopes
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.3")

    // Paging 3
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")

    // Timber Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Dagger Core
    implementation("com.google.dagger:dagger:2.57")
    kapt("com.google.dagger:dagger-compiler:2.57")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.57")
    kapt("com.google.dagger:hilt-android-compiler:2.57")

    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Activity KTX for viewModels()
    implementation("androidx.activity:activity-ktx:1.10.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose")

    // jBCrypt for Password Hashing
    implementation("org.mindrot", "jbcrypt", "0.4")

    // Gson
    implementation("com.google.code.gson", "gson", "2.13.1")

    // Androidx ExifInterface
    implementation("androidx.exifinterface", "exifinterface", "1.4.1")

    // Telephoto
    implementation("me.saket.telephoto:zoomable-image-coil:0.16.0")

    // Coil
    val coilVersion = "2.7.0"
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")
    implementation("io.coil-kt:coil-video:$coilVersion")

    // Exoplayer
    implementation("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")

    implementation(fileTree("libs").matching {
        include("*.jar")
    })
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.preference:preference-ktx:1.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
