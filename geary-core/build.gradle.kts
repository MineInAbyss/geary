plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.stately.concurrency)
                implementation(libs.androidx.collection)
                implementation(idofrontLibs.kotlin.reflect)

                api(libs.koin.core)
                api(idofrontLibs.kotlinx.io)
                api(idofrontLibs.kermit)
                api(idofrontLibs.kotlinx.coroutines)
            }

        }

        val jvmTest by getting {
            dependencies {
                implementation(project(":geary-test"))
                implementation(kotlin("test"))
                implementation(idofrontLibs.kotlinx.coroutines.test)
                implementation(idofrontLibs.kotest.assertions)
                implementation(idofrontLibs.kotest.property)
                implementation(libs.koin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(idofrontLibs.kotlinx.serialization.kaml)
                implementation(idofrontLibs.fastutil)
                implementation(libs.roaringbitmap)
            }
        }
    }
}
