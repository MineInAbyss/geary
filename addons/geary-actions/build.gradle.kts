plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":geary-core"))
                implementation(project(":geary-serialization"))

                implementation(libs.uuid)
                implementation(idofrontLibs.idofront.di)
            }
        }
    }
}
