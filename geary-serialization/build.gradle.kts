@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.multiplatform.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                compileOnly(mylibs.okio)
                implementation(mylibs.uuid)

                compileOnly(libs.kotlinx.serialization.json)
                compileOnly(libs.kotlinx.serialization.cbor)

                compileOnly(project(":geary-core"))
                implementation(libs.idofront.di)
            }
        }
        jvmMain {
            dependencies {
                compileOnly(libs.kotlinx.serialization.kaml)
            }
        }
    }
}
