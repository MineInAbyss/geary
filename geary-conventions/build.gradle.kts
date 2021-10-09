import org.gradle.api.Project.GRADLE_PROPERTIES
import java.util.Properties

// Load properties from root gradle.properties
Properties().apply { load(rootDir.toPath().resolveSibling(GRADLE_PROPERTIES).toFile().inputStream()) }
    .forEach { (key, value) -> project.ext["$key"] = value }

val idofrontConventions: String by project

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    //Get Kotlin plugin version via conventions plugin version
    implementation("com.mineinabyss.conventions.kotlin:com.mineinabyss.conventions.kotlin.gradle.plugin:$idofrontConventions")
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("serialization"))
//    implementation("io.github.slimjar:gradle-plugin")
//    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow")
}
