import com.android.sdklib.AndroidVersion.VersionCodes

plugins {
    id("com.android.application")
    id("com.jaredsburrows.license")
    kotlin("android")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

val appVersionName: String by project
val appVersionCode: String by project

apply(plugin = "androidx.navigation.safeargs.kotlin")
apply(plugin = "dagger.hilt.android.plugin")

android {
    compileSdk = 34 // Android 14

    defaultConfig {
        applicationId = "dev.leonlatsch.photok"
        minSdk = VersionCodes.N
        targetSdk = 34 // Android 14
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
    copyHtmlReportToAssets = false
    copyJsonReportToAssets = true
}

dependencies {
    val roomVersion = "2.6.1"
    val coroutinesVersion = "1.8.1"
    val pagingVersion = "3.3.2"
    val daggerVersion = "2.52"

    // Architectural Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Coroutine Lifecycle Scopes
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Paging 3
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")

    // Timber Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Dagger Core
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:$daggerVersion")
    kapt("com.google.dagger:hilt-android-compiler:$daggerVersion")

    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Activity KTX for viewModels()
    implementation("androidx.activity:activity-ktx:1.9.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.08.00"))
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Easy Permissions
    implementation("pub.devrel:easypermissions:3.0.0")

    // jBCrypt for Password Hashing
    implementation("org.mindrot", "jbcrypt", "0.4")

    // Gson
    implementation("com.google.code.gson", "gson", "2.8.6")

    // Androidx ExifInterface
    implementation("androidx.exifinterface", "exifinterface", "1.3.0-alpha01")

    // Display OSS Licenses
    implementation("com.github.leonlatsch:OssLicenseView:1.1.0")

    // Telephoto
    implementation("me.saket.telephoto:zoomable-image-coil:0.13.0")

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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.9.1")
    implementation("androidx.preference:preference-ktx:1.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
