pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
    }
}

rootProject.name = "geary"

include(
    "geary-spigot",
    "geary-core",
    "geary-components"
)

project(":geary-spigot").projectDir = file("./spigot")
project(":geary-core").projectDir = file("./core")
project(":geary-components").projectDir = file("./components")
