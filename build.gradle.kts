import java.util.*

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion = "1.4.21"
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.2")
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

tasks.register("updateTranslations") {
    val resPath = "app/src/main/res"
    val bytes = java.io.FileInputStream(File("$resPath/values/strings.xml")).readBytes()
    val enLines = String(bytes).split("\n")

    var enStrings = 0

    for (line in enLines) {
        if (line.contains("<string")) {
            enStrings++
        }
    }

    val badges = arrayListOf<String>()
    File(resPath).walk().forEach { dir ->
        if (dir.isDirectory &&
            dir.name.contains("values") &&
            dir.name != "values"
        ) {
            dir.walk().forEach { stringsFile ->
                if (stringsFile.name == "strings.xml") {
                    var strings = 0
                    val lines = String(java.io.FileInputStream(stringsFile).readBytes()).split("\n")
                    for (line in lines) {
                        if (line.contains("<string") && !line.contains("TODO")) {
                            strings++
                        }
                    }
                    val localeName = dir.name.replace("values-", "")
                    val percentage = (strings.toDouble() / enStrings.toDouble()) * 100
                    val template = "https://img.shields.io/badge/{locale}-{percentage}{color}"
                    val color = when {
                        percentage > 99 -> "25-brightgreen"
                        percentage > 75 -> "25-yellow"
                        percentage > 50 -> "25-orange"
                        percentage > 0 -> "25-red"
                        else -> "lightgrey"
                    }
                    val localeDisplay = Locale.forLanguageTag(localeName.replace("-r", "-"))
                        .getDisplayName(Locale.US)
                    val badge = template
                        .replace("{locale}", localeDisplay.replace(" ", "%20"))
                        .replace("{percentage}", "${percentage.toInt()}%")
                        .replace("{color}", color)
                    badges.add(badge)
                }
            }
        }
    } // READ strings.xml

    if (badges.isNotEmpty()) {
        val readmeString = String(java.io.FileInputStream(File("README.md")).readBytes())
        val readmeLines = readmeString.split("\n")

        var beginIndex = 0
        var endIndex = 0
        var i = 0
        while (i < readmeLines.size) {
            if (readmeLines[i].contains("BEGIN-TRANSLATIONS")) {
                beginIndex = i + 1
            }
            if (readmeLines[i].contains("END-TRANSLATIONS")) {
                endIndex = i
            }
            i++
        }

        val prefixStrings = readmeLines.subList(0, beginIndex)
        val suffixStrings = readmeLines.subList(endIndex, readmeLines.size - 1)

        var badgeString = ""
        badges.forEach {
            badgeString += "![]($it)\n"
        }

        var newReadmeString = ""
        prefixStrings.forEach {
            newReadmeString += "$it\n"
        }

        newReadmeString += badgeString

        suffixStrings.forEach {
            newReadmeString += "$it\n"
        }

        if (newReadmeString.isNotEmpty()) {
            File("README.md").writeText(newReadmeString)
        }
    }
}