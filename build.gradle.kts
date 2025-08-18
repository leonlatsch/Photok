buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.12.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.3")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.57")
        classpath("com.jaredsburrows:gradle-license-plugin:0.9.8")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

apply("./gradle/updateVersion.gradle.kts")
apply("./gradle/updateTranslations.gradle.kts")