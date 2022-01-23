import org.jetbrains.compose.compose

val serverVersion: String by project

plugins {
    java
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.platform")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
    id("org.jetbrains.compose") version "1.0.1"
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
                compileOnly(libs.kotlin.stdlib)
                compileOnly(libs.ktor.serialization)
                compileOnly(libs.ktor.server.core)
                compileOnly(libs.ktor.server.netty)
                compileOnly(libs.logback.classic)
                compileOnly(libs.kmongo.coroutine.serialization)
                compileOnly(project(":geary-platform-papermc"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.json)
                implementation(libs.ktor.client.serialization)
                implementation(compose.web.core)
                implementation(compose.runtime)
            }
        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}
