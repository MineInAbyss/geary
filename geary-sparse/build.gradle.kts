plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.androidx.collection)
                implementation(project(":geary-datastructures"))
            }
            kotlin.setSrcDirs(files("src"))
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test"))
            }
            kotlin.setSrcDirs(files("test@jvm"))
            resources.setSrcDirs(files("resources"))
        }

        jvmMain {
            kotlin.setSrcDirs(files("src@jvm"))
        }

        nativeMain {
            kotlin.setSrcDirs(files("src@native"))
        }
    }
}
