import Com_mineinabyss_conventions_platform_gradle.Deps

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
    api(project(":geary-addon"))
    api(project(":geary-autoscan"))
    api(project(":geary-core"))
    api(project(":geary-prefabs"))

    // MineInAbyss platform
    compileOnly(Deps.kotlinx.coroutines)
    compileOnly(Deps.kotlin.reflect) { isTransitive = false }
    compileOnly(libs.reflections)
    compileOnly(Deps.minecraft.skedule)
}
