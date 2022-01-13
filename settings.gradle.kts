rootProject.name = "geary"

pluginManagement {
    val kotlinVersion: String by settings
    val idofrontConventions: String by settings
    val dokkaVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.mineinabyss.conventions"))
                useVersion(idofrontConventions)
        }
    }
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
    }
}


include(
    "geary-core",
    "geary-prefabs",
    "geary-platform-papermc",
)

project(":geary-platform-papermc").projectDir = file("./platforms/geary-platform-papermc")

includeBuild("geary-conventions")
