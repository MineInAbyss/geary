plugins {
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    implementation(libs.reflections)
    implementation(libs.kotlin.reflect)
    implementation("com.mineinabyss:ding:1.0.0")
    compileOnly(libs.idofront.autoscan)
    compileOnly(project(":geary-core"))
//    compileOnly(project(":geary-prefabs"))
}
