val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
    id("geary.kotlin-conventions")
}

dependencies {
//    slim("org.jetbrains.kotlinx:kotlinx-serialization-cbor")
    implementation("com.mineinabyss:idofront:$idofrontVersion")

    //ecs-related libs
    implementation("com.uchuhimo:kotlinx-bimap:1.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("net.onedaybeard.bitvector:bitvector-jvm:0.1.4")

    //provided by Minecraft
    compileOnly("it.unimi.dsi:fastutil:8.5.4")
    testImplementation("it.unimi.dsi:fastutil:8.5.4")
}
