import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${mylibs.versions.atomicfu.get()}")
    }
}

apply(plugin = "kotlinx-atomicfu")

repositories {
    mavenCentral()
    google()
}

//TODO dev options to only build for one target at a time
fun KotlinTarget.disableCompilations() {
    compilations.configureEach {
        compileKotlinTask.enabled = false
    }
}

kotlin {
    targets.configureEach {
        if (name == KotlinMultiplatformPlugin.METADATA_TARGET_NAME) return@configureEach
        if (platformType != KotlinPlatformType.jvm)
            disableCompilations()
    }

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
//    js(IR) {
//        browser()
//        nodejs()
//    }
    sourceSets {
        all {
            explicitApi()
            languageSettings {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.RequiresOptIn")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(mylibs.atomicfu)
                implementation(mylibs.uuid)
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.serialization.cbor)
                implementation("com.mineinabyss:ding:1.0.0")

                api(mylibs.okio)
                api(mylibs.kds)
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.serialization.json)
            }

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.property)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.kaml)
                implementation(mylibs.roaringbitmap)
            }
        }
        val jvmTest by getting

//        val jsMain by getting {
//            dependencies {
//                api(mylibs.bitvector.js)
//            }
//        }
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
