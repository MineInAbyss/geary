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
    val atomicfuVersion = "0.17.1"

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfuVersion")
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
    js(IR) {
        browser()
        nodejs()
    }
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
                implementation("org.jetbrains.kotlinx:atomicfu:0.17.1")
                implementation("com.benasher44:uuid:0.4.0")
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.serialization.cbor)
//                implementation(gearylibs.bitvector)
//                api(libs.kotlinx.coroutines)
                api("com.squareup.okio:okio:3.0.0")
                api(libs.koin.core)// { isTransitive = false }
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                api(libs.kotlinx.serialization.json)
                api("com.soywiz.korlibs.kds:kds:2.2.1")
//                implementation(libs.koin.test.junit5)
//                implementation("io.kotest:kotest-runner-junit5:5.2.3")
            }

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.koin.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")
                implementation("io.kotest:kotest-assertions-core:5.2.3")
                implementation("io.kotest:kotest-property:5.2.3")

//                implementation("io.insert-koin:koin-test:3.1.5")
//                implementation(libs.koin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.kaml)
                implementation("org.roaringbitmap:RoaringBitmap:0.9.25")
            }
        }
        val jvmTest by getting

        val jsMain by getting {
            dependencies {
                api(gearylibs.bitvector.js)
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
