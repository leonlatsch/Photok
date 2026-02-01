buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.7")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.59")
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