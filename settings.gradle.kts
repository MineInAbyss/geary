pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
    }
}

rootProject.name = "geary"

include("geary-spigot")

project(":geary-spigot").projectDir = file("./spigot")
