plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

kotlin {
    targets {

    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(libs.okio)
                compileOnly(libs.uuid)
                compileOnly(idofrontLibs.kotlinx.serialization.json)
                compileOnly(idofrontLibs.kotlinx.serialization.cbor)

                compileOnly(project(":geary-core"))
                compileOnly(project(":geary-serialization"))
                implementation(idofrontLibs.idofront.di)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(idofrontLibs.kotlinx.coroutines.test)
                implementation(idofrontLibs.kotest.assertions)
                implementation(idofrontLibs.kotest.property)
                implementation(idofrontLibs.idofront.di)
                implementation(project(":geary-core"))
                implementation(project(":geary-serialization"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(idofrontLibs.junit.jupiter)
            }
        }
    }
}
