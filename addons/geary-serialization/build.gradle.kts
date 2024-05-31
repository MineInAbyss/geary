plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":geary-core"))

                implementation(libs.uuid)
                implementation(idofrontLibs.idofront.di)

                api(idofrontLibs.kotlinx.serialization.cbor)
                api(idofrontLibs.kotlinx.serialization.json)
                api(libs.okio)
            }
        }
        jvmMain {
            dependencies {
                implementation(idofrontLibs.kotlinx.serialization.kaml)
            }
        }
    }
}
