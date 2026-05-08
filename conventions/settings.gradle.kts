import java.util.*

dependencyResolutionManagement {
    val gradlePropertiesFile = file("../gradle.properties")
    val properties = Properties()
    gradlePropertiesFile.inputStream().use { properties.load(it) }
    val catalogVersion = properties.getProperty("catalogVersion")
    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }
    versionCatalogs {
        create("idofrontLibs") {
            from("com.mineinabyss:catalog:$catalogVersion")
        }
    }
}