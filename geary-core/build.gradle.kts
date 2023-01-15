import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
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
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(mylibs.atomicfu)
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(libs.idofront.di)

                api(mylibs.kds)
                api(mylibs.kermit)
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
                implementation(libs.idofront.di)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.kaml)
                implementation(mylibs.roaringbitmap)
            }
        }
        val jsMain by getting {
            dependencies {
                //TODO library for js bitsets
            }
        }
    }
}
