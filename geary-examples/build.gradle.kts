plugins {
    id(idofrontLibs.plugins.mia.kotlin.multiplatform.get().pluginId)
    id("multiplatform-targets")
    alias(idofrontLibs.plugins.kotlinx.serialization)
}


kotlin {
    jvm()
    sourceSets {
        jvmTest {
            dependencies {
                implementation(project(":geary-core"))
                implementation(project(":geary-test"))
                implementation(kotlin("test"))
                implementation(idofrontLibs.kotlinx.coroutines.test)
//                compileOnly(idofrontLibs.junit.jupiter)
            }
        }
    }
}