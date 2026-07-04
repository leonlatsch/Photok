include(":app")
rootProject.name = "Photok"

if (file("pro/build.gradle.kts").exists()) {
    include(":pro")
}
