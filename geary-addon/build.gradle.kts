plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    implementation(libs.reflections)
    implementation(libs.kotlin.reflect)
    compileOnly(project(":geary-core"))
    compileOnly(project(":geary-prefabs"))
}
