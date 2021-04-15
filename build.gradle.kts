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
    id("com.mineinabyss.shared-gradle") version "0.0.2"
}

val pluginVersion: String by project

allprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.dokka")
//    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")

    kotlin {
        explicitApi()
    }

    repositories {
        mavenCentral()
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

    //ecs-related libs
    implementation("net.onedaybeard.bitvector:bitvector-jvm:0.1.4")
    implementation("com.zaxxer:SparseBitSet:1.1")
    implementation("org.clapper:javautil:3.2.0")
    implementation("fastutil:fastutil:5.0.9")
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
