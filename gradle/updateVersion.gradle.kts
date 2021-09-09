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

tasks.register("updateVersion") {
    doLast {
        val version: String? by project
        if (version == null || version == "Unspecified") {
            return@doLast
        }

        val file = file("gradle.properties")
        if (file.canRead()) {
            val properties = Properties().apply {
                load(file.inputStream())
            }

            val oldVersionCode: String = properties["appVersionCode"] as String
            val newVersionCode: String = oldVersionCode.toInt().inc().toString()

            properties["appVersionName"] = version!!
            properties["appVersionCode"] = newVersionCode

            properties.store(file.writer(), null)
        } else {
            throw GradleException("${file.name} not readable")
        }
    }
}