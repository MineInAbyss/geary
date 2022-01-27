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
            alias("plugman").to("com.rylinaux:PlugMan:2.2.5")
        }
    }
}

include(
    "geary-autoscan",
    "geary-addon",
    "geary-core",
    "geary-prefabs",
    "geary-web-console",
    "geary-papermc",
)

project(":geary-papermc").projectDir = file("./platforms/papermc")

file("./platforms/papermc")
    .listFiles()!!
    .filter { it.isDirectory && it.name !in setOf("src", "build") }
    .forEach {
        val name = "geary-papermc-${it.name}"
        include(name)
        project(":$name").projectDir = it
    }

includeBuild("geary-conventions")
includeBuild("../Idofront")
