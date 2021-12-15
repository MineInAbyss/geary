import Com_mineinabyss_conventions_platform_gradle.Deps

plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    //ecs-related libs
    implementation("com.uchuhimo:kotlinx-bimap:1.2") { isTransitive = false }
    implementation("net.onedaybeard.bitvector:bitvector-jvm:0.1.4")
    implementation(Deps.kotlin.reflect) { isTransitive = false }

    //provided by Minecraft
    // TODO implementation here, avoid shading on papermc
    compileOnly("it.unimi.dsi:fastutil:8.5.4")
    testImplementation("it.unimi.dsi:fastutil:8.5.4")
}
