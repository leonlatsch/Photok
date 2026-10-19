import java.util.*

/*
 *   Copyright 2020–2026 Leon Latsch
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
    val resPath = "core/src/main/res"

    val badges = arrayListOf<String>()
    File(resPath).listFiles().orEmpty().forEach { dir ->
        if (dir.isDirectory &&
            dir.name.startsWith("values-") &&
            File(dir, "strings.xml").exists()
        ) {
            val localeName = dir.name.removePrefix("values-")
            val localeDisplay = Locale.forLanguageTag(localeName.replace("-r", "-"))
                .getDisplayName(Locale.US)
            badges.add("![$localeDisplay](https://img.shields.io/badge/${localeDisplay.replace(" ", "%20")}-blue)\n")
        }
    }

    if (badges.isNotEmpty()) {
        badges.sort()

        val readmeLines = File("README.md").readText().split("\n")

        var beginIndex = 0
        var endIndex = 0
        readmeLines.forEachIndexed { i, line ->
            if (line.contains("BEGIN-TRANSLATIONS")) {
                beginIndex = i + 1
            }
            if (line.contains("END-TRANSLATIONS")) {
                endIndex = i
            }
        }

        val prefixStrings = readmeLines.subList(0, beginIndex)
        val suffixStrings = readmeLines.subList(endIndex, readmeLines.size - 1)

        var newReadmeString = ""
        prefixStrings.forEach {
            newReadmeString += "$it\n"
        }

        newReadmeString += "![English](https://img.shields.io/badge/English-blue)\n"
        badges.forEach {
            newReadmeString += it
        }

        suffixStrings.forEach {
            newReadmeString += "$it\n"
        }

        File("README.md").writeText(newReadmeString)
    }
}
