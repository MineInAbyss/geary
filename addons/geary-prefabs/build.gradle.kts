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
                implementation(project(":geary-serialization"))
            }
            kotlin.srcDir("src")
        }

        jvmMain {
            kotlin.srcDir("src@jvm")
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":geary-test"))
                implementation(idofrontLibs.kotlinx.coroutines.test)
                implementation(idofrontLibs.kotest.assertions)
                implementation(idofrontLibs.kotest.property)
                implementation(project(":geary-core"))
                implementation(project(":geary-serialization"))
                implementation(idofrontLibs.junit.jupiter)
            }
            kotlin.srcDir("test@jvm")
            resources.srcDir("resources")
        }
    }
}
