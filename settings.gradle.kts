rootProject.name = "geary"
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    val kotlinVersion: String by settings
    val idofrontConventions: String by settings
    val dokkaVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
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
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
    }

    versionCatalogs {
        create("libs") {
            from("com.mineinabyss:catalog:1.6.10-DEV")
        }
        create("gearylibs") {
            version("bimap-test", "1.2")
            alias("bimap").to("com.uchuhimo", "kotlinx-bimap").versionRef("bimap-test")
            alias("bitvector").to("net.onedaybeard.bitvector:bitvector-jvm:0.1.4")
            alias("fastutil").to("it.unimi.dsi:fastutil:8.2.2") //Version on minecraft server
            alias("reflections").to("org.reflections:reflections:0.9.12")
            alias("plugman").to("com.rylinaux:PlugMan:2.2.5")
        }
    }
}

include(
    "geary-core",
    "geary-prefabs",
    "geary-web-console",
    "geary-platform-papermc",
    "geary-platform-papermc-core",
    "geary-platform-papermc-plugin",
)

project(":geary-platform-papermc").projectDir = file("./platforms/geary-platform-papermc")
project(":geary-platform-papermc-core").projectDir = file("./platforms/geary-platform-papermc/core")
project(":geary-platform-papermc-plugin").projectDir = file("./platforms/geary-platform-papermc/plugin")

includeBuild("geary-conventions")
includeBuild("../Idofront")
