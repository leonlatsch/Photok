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
                    var author = "UNKNOWN"
                    val lines = String(java.io.FileInputStream(stringsFile).readBytes()).split("\n")
                    for (line in lines) {
                        if (line.contains("<string") && !line.contains("TODO")) {
                            strings++
                        } else if (line.contains("MAINTAINED BY")) {
                            author = line.substring(line.indexOf("(") + 1, line.indexOf(")"))
                        }
                    }
                    val localeName = dir.name.replace("values-", "")
                    val percentage = (strings.toDouble() / enStrings.toDouble()) * 100
                    val template =
                        "![{alt-locale}](https://img.shields.io/badge/{locale}-{percentage}{color})\nMaintained by {author}\n\n"
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
                        .replace("{alt-locale}", localeDisplay)
                        .replace("{percentage}", "${percentage.toInt()}%")
                        .replace("{color}", color)
                        .replace("{author}", author)
                    badges.add(badge)
                }
            }
        }
    } // READ strings.xml

    if (badges.isNotEmpty()) {
        badges.sort()

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
        badgeString += "![English](https://img.shields.io/badge/English-100%25-brightgreen)\nMaintained by @leonlatsch\n\n" // Hard code add english
        badges.forEach {
            badgeString += it
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