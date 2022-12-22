plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                compileOnly(libs.kotlinx.serialization.json)
                compileOnly(libs.kotlinx.serialization.cbor)

                compileOnly(project(":geary-core"))
                implementation("com.mineinabyss:ding:1.0.0")
            }
        }
        jvmMain {
            dependencies {
                compileOnly(libs.kotlinx.serialization.kaml)
            }
        }
    }
}
