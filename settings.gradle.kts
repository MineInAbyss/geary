rootProject.name = "geary"

pluginManagement {
    val kotlinVersion: String by settings
    val idofrontVersion: String by settings
    val dokkaVersion: String by settings

    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs").from("com.mineinabyss:catalog:$idofrontVersion")
        create("mylibs").from(files("gradle/mylibs.versions.toml"))
    }
}

include(
    "geary-core",
    "geary-autoscan",
    "geary-prefabs",
    "geary-serialization",
    "geary-uuid",
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
