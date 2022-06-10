import org.gradle.api.Project.GRADLE_PROPERTIES

dependencyResolutionManagement {
    val idofrontVersion by java.util.Properties().apply { load(file("../$GRADLE_PROPERTIES").inputStream()) }
    repositories {
        mavenLocal()
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs").from("com.mineinabyss:catalog:$idofrontVersion")
    }
}
