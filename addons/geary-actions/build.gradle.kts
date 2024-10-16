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
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":geary-test"))
                implementation(kotlin("test"))
                implementation(idofrontLibs.kotlinx.coroutines.test)
                implementation(idofrontLibs.kotlinx.serialization.kaml)
                implementation(idofrontLibs.kotest.assertions)
                implementation(idofrontLibs.kotest.property)
                implementation(project(":geary-core"))
                implementation(project(":geary-serialization"))
            }
        }
    }
}
