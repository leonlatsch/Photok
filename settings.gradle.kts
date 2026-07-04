include(":app")
include(":core")
rootProject.name = "Photok"

if (file("pro/build.gradle.kts").exists()) {
    include(":pro")
}
