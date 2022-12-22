plugins {
    kotlin("multiplatform")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(mylibs.uuid)
                compileOnly(project(":geary-core"))
                implementation("com.mineinabyss:ding:1.0.0")
            }
        }
    }
}
