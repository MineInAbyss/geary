import com.mineinabyss.geary.Deps
import com.mineinabyss.kotlinSpice
import com.mineinabyss.mineInAbyss
import com.mineinabyss.sharedSetup
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    `maven-publish`

    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("jvm") version com.mineinabyss.geary.Deps.kotlinVersion
    kotlin("plugin.serialization") version com.mineinabyss.geary.Deps.kotlinVersion
    id("org.jetbrains.dokka") version "1.4.32"
    id("com.mineinabyss.shared-gradle") version "0.0.6"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlinx-serialization")

    sharedSetup {
        addGithubRunNumber()
        applyJavaDefaults()
    }

    kotlin {
        explicitApi()
    }

    repositories {
        mavenCentral()
        jcenter()
        mineInAbyss()
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))

        kotlinSpice("${Deps.kotlinVersion}+")
        compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-cbor")

        implementation("com.mineinabyss:idofront:0.6.13")
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

        test {
            useJUnitPlatform()
        }
    }
}

tasks {
    build {
        dependsOn(project(":geary-spigot").tasks.build)
    }
}

publishing {
    mineInAbyss(project) {
        from(components["java"])
    }
}
