import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${libs.versions.atomicfu.get()}")
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
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.atomicfu)
                implementation(idofrontLibs.kotlin.reflect)
                implementation(idofrontLibs.kotlinx.serialization.cbor)

                api(idofrontLibs.idofront.di)
                api(libs.kermit)
                api(idofrontLibs.kotlinx.coroutines)
                api(idofrontLibs.kotlinx.serialization.json)
            }

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(idofrontLibs.kotlinx.coroutines.test)
                implementation(idofrontLibs.kotest.assertions)
                implementation(idofrontLibs.kotest.property)
                implementation(idofrontLibs.idofront.di)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(idofrontLibs.kotlinx.serialization.kaml)
                implementation(idofrontLibs.fastutil)
                implementation(libs.roaringbitmap)
            }
        }
        val jsMain by getting {
            dependencies {
                //TODO library for js bitsets
            }
        }
    }
}
