import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("io.github.slimjar")
}

repositories {
    mavenCentral()
}

dependencies {
    slim(platform("com.mineinabyss:kotlinspice:${kotlinVersion}+"))

    slim(kotlin("stdlib-jdk8"))
    slim("org.jetbrains.kotlinx:kotlinx-serialization-json")
    slim("org.jetbrains.kotlinx:kotlinx-serialization-cbor")
    slim("com.charleskorn.kaml:kaml") {
        exclude(group = "org.jetbrains.kotlin")
    }
}

kotlin {
    explicitApi()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xinline-classes",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
}
