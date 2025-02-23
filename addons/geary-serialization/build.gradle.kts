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

                api(idofrontLibs.kotlinx.serialization.cbor)
                api(idofrontLibs.kotlinx.serialization.json)
                api(idofrontLibs.kotlinx.serialization.kaml)
                api(idofrontLibs.kotlinx.io)
            }
        }
    }
}
