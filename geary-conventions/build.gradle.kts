import org.gradle.api.Project.GRADLE_PROPERTIES
import java.util.*

// Load properties from root gradle.properties
Properties().apply { load(rootDir.toPath().resolveSibling(GRADLE_PROPERTIES).toFile().inputStream()) }
    .forEach { (key, value) -> project.ext["$key"] = value }

val idofrontVersion: String by project
val kotlinVersion: String by project

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://papermc.io/repo/repository/maven-public/")
    mavenLocal()
}

dependencies {
    //Get Kotlin plugin version via conventions plugin version
    //TODO this is terrible
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation("com.mineinabyss.conventions.kotlin:com.mineinabyss.conventions.kotlin.gradle.plugin:$idofrontVersion")
    implementation(kotlin("gradle-plugin", version = kotlinVersion))
    implementation(kotlin("serialization", version = kotlinVersion))
}
