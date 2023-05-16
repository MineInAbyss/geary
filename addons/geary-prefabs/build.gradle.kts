@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    targets {

    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(mylibs.okio)
                compileOnly(mylibs.uuid)
                compileOnly(libs.kotlinx.serialization.json)
                compileOnly(libs.kotlinx.serialization.cbor)

                compileOnly(project(":geary-core"))
                compileOnly(project(":geary-serialization"))
                implementation(libs.idofront.di)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.property)
                implementation(libs.idofront.di)
                implementation(project(":geary-core"))
                implementation(project(":geary-serialization"))
            }
        }
    }
}
