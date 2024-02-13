plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":geary-core"))

                api(libs.uuid)
                implementation(libs.atomicfu)
                implementation(idofrontLibs.idofront.di)
            }
        }
    }
}
