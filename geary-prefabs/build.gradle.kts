plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

kotlin {
    targets {

    }
    sourceSets {
        commonMain {
            dependencies {
                compileOnly(mylibs.okio)
                compileOnly(libs.kotlinx.serialization.json)
                compileOnly(libs.kotlinx.serialization.cbor)

                compileOnly(project(":geary-core"))
                compileOnly(project(":geary-serialization"))
                implementation("com.mineinabyss:ding:1.0.0")
            }
        }
    }
}
