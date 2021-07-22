import org.gradle.api.Project.GRADLE_PROPERTIES
import java.util.Properties

// Load properties from root gradle.properties
Properties().apply { load(rootDir.toPath().resolveSibling(GRADLE_PROPERTIES).toFile().inputStream()) }
    .forEach { (key, value) -> project.ext["$key"] = value }

val kotlinVersion: String by project

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("serialization", kotlinVersion))
    implementation("io.github.slimjar:gradle-plugin:1.2.1")
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
}
