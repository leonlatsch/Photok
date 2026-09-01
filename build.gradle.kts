buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.1.1")
        classpath("com.android.tools.build:gradle-kotlin:9.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.11")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.10.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.60.1")
        classpath("com.jaredsburrows:gradle-license-plugin:0.9.9")
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