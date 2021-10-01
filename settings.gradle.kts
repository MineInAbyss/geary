rootProject.name = "geary"

pluginManagement {
    val idofrontConventions: String by settings

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
}

include(
    "geary-core",
    "geary-platform-papermc",
)

project(":geary-platform-papermc").projectDir = file("./platforms/geary-platform-papermc")
