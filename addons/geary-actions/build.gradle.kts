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
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(idofrontLibs.kotlinx.coroutines.test)
                implementation(idofrontLibs.kotest.assertions)
                implementation(idofrontLibs.kotest.property)
                implementation(idofrontLibs.idofront.di)
                implementation(project(":geary-core"))
                implementation(project(":geary-serialization"))
            }
        }
    }
}
