rootProject.name = "geary"

pluginManagement {

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
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
