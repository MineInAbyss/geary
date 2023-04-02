rootProject.name = "geary"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
//        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs") {
            from("com.mineinabyss:catalog:$idofrontVersion")
            // Version overrides
            version("kotlin", "1.8.10")
            version("reflections", "0.10.2")
        }
        create("mylibs").from(files("gradle/mylibs.versions.toml"))
    }
}

include(
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
