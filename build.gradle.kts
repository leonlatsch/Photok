// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion = "1.4.21"
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
        classpath("com.jaredsburrows:gradle-license-plugin:0.8.80")
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

tasks {
    task<Delete>("clean") {
        delete(rootProject.buildDir)
    }

    task("licenseAndCreditsReport") {
        dependsOn("app:licenseReleaseReport")
        group = "reporting"
        doLast {
            println("Copying Icon Credits to license report file... ")
            var bodyEnd = 0
            val licFile = File("app/src/main/assets/open_source_licenses.html")
            val licensesLines = licFile.readLines().toMutableList()
            licensesLines.forEachIndexed { index, line ->
                if (line.contains("</body>")) {
                    bodyEnd = index
                }
            }

            val credits = File("icon_credits.html").readText()
            licensesLines.add(bodyEnd, credits)

            licFile.writeText(licensesLines.joinToString("\n"))

            println("Done")
        }
    }
}
