import com.mineinabyss.mineInAbyss
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.mineinabyss.shared-gradle")
    id("com.github.johnrengelman.shadow")
    id("io.github.slimjar")
}

repositories {
    mavenCentral()
    mineInAbyss()
}

dependencies {
    slim(kotlin("stdlib-jdk8"))

    slim(platform("com.mineinabyss:kotlinspice:${kotlinVersion}+"))
    slim("org.jetbrains.kotlinx:kotlinx-serialization-cbor")
    slim("com.charleskorn.kaml:kaml:0.34.0") {
        exclude(group = "org.jetbrains.kotlin")
    }

}

kotlin {
    explicitApi()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf(
                "-Xinline-classes",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
}
