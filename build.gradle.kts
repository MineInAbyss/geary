import com.mineinabyss.kotlinSpice
import com.mineinabyss.mineInAbyss
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    `maven-publish`

    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    id("org.jetbrains.dokka") version "1.4.30"
    id("com.mineinabyss.shared-gradle") version "0.0.3"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")

    kotlin {
        explicitApi()
    }

    repositories {
        mavenCentral()
        jcenter()
        mineInAbyss()
    }

    dependencies {
        kotlinSpice("${Versions.kotlin}+")
        compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-cbor")

        implementation("com.mineinabyss:idofront:0.5.8")
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
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))

    //provided by Minecraft
    compileOnly("fastutil:fastutil:5.0.9")

    //ecs-related libs
    implementation("com.uchuhimo:kotlinx-bimap:1.2") {
        exclude(group = "org.jetbrains.kotlin")
    }

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.kotest:kotest-runner-junit5:4.4.2")
    testImplementation("io.kotest:kotest-property:4.4.2")
    testImplementation("io.mockk:mockk:1.10.6")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    mineInAbyss(project) {
        artifactId = "geary"
        from(components["java"])
    }
}
