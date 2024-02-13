plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                compileOnly(project(":geary-core"))
                compileOnly(idofrontLibs.kotlinx.serialization.json)
                compileOnly(idofrontLibs.kotlinx.serialization.cbor)

                implementation(libs.uuid)
                implementation(idofrontLibs.idofront.di)

                api(libs.okio)
            }
        }
        jvmMain {
            dependencies {
                compileOnly(idofrontLibs.kotlinx.serialization.kaml)
            }
        }
    }
}
