plugins {
    id("geary.kotlin-conventions")
//    kotlin("plugin.serialization")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

dependencies {
    api(libs.reflections)
    compileOnly(project(":geary-core"))
}
