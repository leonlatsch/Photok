// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion = "2.1.10"// Don't update. See https://issuetracker.google.com/u/1/issues/386304679?pli=1
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.6")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.55")
        classpath("com.jaredsburrows:gradle-license-plugin:0.9.8")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
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