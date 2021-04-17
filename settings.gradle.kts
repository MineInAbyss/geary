pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
    }
}

rootProject.name = "geary"

include(
    "geary-core",
    "geary-components",
    "geary-spigot",
    "geary-spigot:geary-spigot-core",
    "geary-spigot:geary-spigot-components",
)

project(":geary-core").projectDir = file("./core")
project(":geary-components").projectDir = file("./components")

project(":geary-spigot").projectDir = file("./spigot")
project(":geary-spigot:geary-spigot-core").projectDir = file("./spigot/core")
project(":geary-spigot:geary-spigot-components").projectDir = file("./spigot/components")
