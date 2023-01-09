@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.multiplatform.get().pluginId)
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(mylibs.uuid)
                compileOnly(project(":geary-core"))
                implementation(libs.idofront.di)
            }
        }
    }
}
