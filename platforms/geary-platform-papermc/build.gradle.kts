plugins {
    id("geary.kotlin-conventions")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.minecraft.skedule)
    compileOnly(libs.kotlin.reflect) { isTransitive = false }

    // Shaded
    api(project(":geary-core")) {
        exclude(module = "kotlin-reflect")
    }
    api(project(":geary-prefabs"))
    api(project(":geary-platform-papermc-core"))
    implementation(gearylibs.reflections)
}
