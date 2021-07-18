val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    slim("org.jetbrains.kotlinx:kotlinx-serialization-cbor")
    implementation("com.mineinabyss:idofront:$idofrontVersion")

    //provided by Minecraft
    compileOnly("it.unimi.dsi:fastutil:8.5.4")
    testImplementation("it.unimi.dsi:fastutil:8.5.4")

    //ecs-related libs
    implementation("com.uchuhimo:kotlinx-bimap:1.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("net.onedaybeard.bitvector:bitvector-jvm:0.1.4")
}
