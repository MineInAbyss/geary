plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":geary-core"))
    api(project(":geary-autoscan"))
    api(project(":geary-prefabs"))
    api(project(":geary-serialization"))
    api(project(":geary-uuid"))
    compileOnly(libs.fastutil)

    // MineInAbyss platform
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.kotlin.reflect) { isTransitive = false }
    compileOnly(libs.reflections)
    compileOnly(libs.minecraft.mccoroutine)

    implementation(libs.idofront.autoscan)
    implementation(libs.bundles.idofront.core)
}
