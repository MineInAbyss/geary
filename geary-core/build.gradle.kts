import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${mylibs.versions.atomicfu.get()}")
    }
}

apply(plugin = "kotlinx-atomicfu")

//TODO dev options to only build for one target at a time
fun KotlinTarget.disableCompilations() {
    compilations.configureEach {
        compileKotlinTask.enabled = false
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(mylibs.atomicfu)
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.serialization.cbor)
                implementation("com.mineinabyss:ding:1.0.0")

                api(mylibs.okio)
                api(mylibs.kds)
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.serialization.json)
            }

        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.property)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.kotlinx.serialization.kaml)
                implementation(mylibs.roaringbitmap)
            }
        }
    }
}

publishing {
    repositories {
        maven("https://repo.mineinabyss.com/releases") {
            credentials {
                username = project.findProperty("mineinabyssMavenUsername") as String?
                password = project.findProperty("mineinabyssMavenPassword") as String?
            }
        }
    }
}
