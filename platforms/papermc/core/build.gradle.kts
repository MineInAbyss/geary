plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.papermc")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.idofront.autoscan)
    api(project(":geary-addon"))
    api(project(":geary-core"))
    api(project(":geary-prefabs"))

    // MineInAbyss platform
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.kotlin.reflect) { isTransitive = false }
    compileOnly(libs.reflections)
}
