import Com_mineinabyss_conventions_platform_gradle.Deps

plugins {
    java
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.platform")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        //TODO potentially version catalogs will help since we can't use platforms here AFAIK
        val commonMain by getting {
            dependencies {
                compileOnly(libs.kotlin.stdlib)
                compileOnly(libs.kotlinx.serialization.json)
                compileOnly(libs.ktor.client.core)
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
                compileOnly(project(":geary-core"))
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
            }
        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}
