plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id("multiplatform-targets")
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":geary-core"))

                implementation(libs.atomicfu)
            }
            kotlin.srcDirs("src")
        }
    }
}
