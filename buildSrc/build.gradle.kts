plugins {
    `kotlin-dsl`
//    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins.register("dependencies") {
        id = "dependencies"
        implementationClass = "com.mineinabyss.DependenciesPlugin"
    }
}
