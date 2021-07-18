rootProject.name = "geary"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.mineinabyss.conventions"))
                useVersion("0.0.6")
        }
    }
}

include(
    "geary-core",
    "geary-platform-papermc",
)

project(":geary-platform-papermc").projectDir = file("./platforms/geary-platform-papermc")
