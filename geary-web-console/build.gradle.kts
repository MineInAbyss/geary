import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.compose

val serverVersion: String by project

plugins {
    java
    kotlin("multiplatform")
    kotlin("plugin.serialization")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev620"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        //TODO potentially version catalogs will help since we can't use platforms here AFAIK
        val commonMain by getting {
            dependencies {
                compileOnly(libs.kotlin.stdlib)
                compileOnly(libs.kotlinx.serialization.json)
                compileOnly(libs.ktor.client.core)
                compileOnly(compose.runtime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                compileOnly("io.papermc.paper:paper-api:$serverVersion")
                compileOnly(project(":geary-core"))
                compileOnly(project(":geary-papermc-core"))
                compileOnly(libs.kotlin.stdlib)
                compileOnly(libs.ktor.serialization)
                compileOnly(libs.ktor.server.core)
                compileOnly(libs.ktor.server.netty)
                compileOnly(libs.logback.classic)
                compileOnly(libs.kmongo.coroutine.serialization)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.json)
                implementation(libs.ktor.client.serialization)
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.material)
            }
        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}
