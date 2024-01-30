plugins {
    id(libs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":geary-core"))

                api(mylibs.uuid)
                implementation(mylibs.atomicfu)
                implementation(libs.idofront.di)
            }
        }
    }
}
