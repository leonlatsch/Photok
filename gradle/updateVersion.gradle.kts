import java.util.*

import java.util.*



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