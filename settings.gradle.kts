rootProject.name = "geary"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
//        maven("https://repo.mineinabyss.com/snapshots")
    }
    includeBuild("conventions")
}

dependencyResolutionManagement {
    val catalogVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
//        maven("https://repo.mineinabyss.com/snapshots")
    }

    versionCatalogs {
        create("idofrontLibs") {
            from("com.mineinabyss:catalog:$catalogVersion")
        }
    }
}

include(
    "geary-benchmarks",
    "geary-core",
    "geary-test",
    "geary-examples",
)

// Go through addons directory and load all projects based on file name
for (addon in file("addons").listFiles()) {
    if (addon.isDirectory) {
        include(addon.name)
        project(":${addon.name}").projectDir = file(addon)
    }
}

gradle.lifecycle.beforeProject {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.mineinabyss.com/releases")
    }
}