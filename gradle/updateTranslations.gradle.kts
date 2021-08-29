import java.util.*

/*
 *   Copyright 2020-2021 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
                        "![{alt-locale}](https://img.shields.io/badge/{locale}-{percentage}{color})\n"
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
        badgeString += "![English](https://img.shields.io/badge/English-100%25-brightgreen)\n" // Hard code add english
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