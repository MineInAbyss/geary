plugins {
    `version-catalog`
    id("com.mineinabyss.conventions.publication")
}

catalog {
    versionCatalog {
        // Add aliases for all our conventions plugins
        rootProject.file("addons").list()
            ?.plus(rootProject.file(".").list()?.filter { it.startsWith("geary") } ?: emptyList())
            ?.forEach { name ->
                library(name, "com.mineinabyss:$name:$version")
            }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}
