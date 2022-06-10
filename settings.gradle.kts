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
        maven("https://repo.papermc.io/repository/maven-public/")
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
        create("gearylibs").from(files("gradle/gearylibs.versions.toml"))
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
