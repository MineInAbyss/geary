plugins {
    id(libs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":geary-core"))

                implementation(mylibs.uuid)
                implementation(libs.idofront.di)
            }
        }
    }
}
