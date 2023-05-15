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
        commonMain {
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
    }
}