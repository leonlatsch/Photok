import java.util.*

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion = "1.4.21"
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
        classpath("com.jaredsburrows:gradle-license-plugin:0.8.90")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
}

apply("./gradle/updateVersion.gradle.kts")
apply("./gradle/updateTranslations.gradle.kts")