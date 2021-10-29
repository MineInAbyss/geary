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
    implementation("org.reflections:reflections:0.9.12")

    slim(Deps.kotlinx.coroutines)
    slim(Deps.minecraft.skedule)
    slim(Deps.kotlin.reflect) { isTransitive = false }
    compileOnly("com.rylinaux:PlugMan:2.2.5")

    api(project(":geary-core"))
}

tasks {
    shadowJar {
        archiveBaseName.set("Geary")
    }
}
