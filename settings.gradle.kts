rootProject.name = "geary"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("libs") {
            from("com.mineinabyss:catalog:$idofrontVersion")
        }
        create("mylibs").from(files("gradle/mylibs.versions.toml"))
    }
}

include(
    "geary-benchmarks",
    "geary-core",
    "geary-catalog",
)

// Go through addons directory and load all projects based on file name
for (addon in file("addons").listFiles()) {
    if (addon.isDirectory) {
        include(addon.name)
        project(":${addon.name}").projectDir = file(addon)
    }
}
