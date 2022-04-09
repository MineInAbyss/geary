rootProject.name = "geary"

pluginManagement {
    val kotlinVersion: String by settings
    val idofrontVersion: String by settings
    val dokkaVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://papermc.io/repo/repository/maven-public/")
        mavenLocal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.mineinabyss.conventions"))
                useVersion(idofrontVersion)
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
    val idofrontVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        mavenLocal()
    }

    versionCatalogs {
        create("libs").from("com.mineinabyss:catalog:$idofrontVersion")
        create("gearylibs") {
            library("bitvector.jvm", "net.onedaybeard.bitvector:bitvector-jvm:0.1.4")
            library("bitvector.js", "net.onedaybeard.bitvector:bitvector-js:0.1.4")
            library("fastutil", "it.unimi.dsi:fastutil:8.2.2") //Version present on minecraft server
            library("plugman", "com.rylinaux:PlugMan:2.2.5")
        }
    }
}

include(
    "geary-addon",
    "geary-core",
    "geary-prefabs",
//    "geary-web-console",
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
