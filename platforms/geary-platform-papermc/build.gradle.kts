import Com_mineinabyss_conventions_platform_gradle.Deps

plugins {
    id("geary.kotlin-conventions")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.copyjar")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://jitpack.io")
    maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
}

dependencies {
    // MineInAbyss platform
    compileOnly(Deps.kotlinx.coroutines)
    compileOnly(Deps.minecraft.skedule)
    compileOnly(Deps.kotlin.reflect) { isTransitive = false }

    // Other plugins
    compileOnly("com.rylinaux:PlugMan:2.2.5")

    // Shaded
    api(project(":geary-core")) {
        exclude(module = "kotlin-reflect")
    }
    api(project(":geary-prefabs"))
    implementation("org.reflections:reflections:0.9.12")
}

tasks {
    shadowJar {
        archiveBaseName.set("Geary")
    }
}
