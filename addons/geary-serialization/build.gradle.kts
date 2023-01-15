@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                compileOnly(project(":geary-core"))
                compileOnly(libs.kotlinx.serialization.json)
                compileOnly(libs.kotlinx.serialization.cbor)

                implementation(mylibs.uuid)
                implementation(libs.idofront.di)

                api(mylibs.okio)
            }
        }
        jvmMain {
            dependencies {
                compileOnly(libs.kotlinx.serialization.kaml)
            }
        }
    }
}
