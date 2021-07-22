val kotlinVersion: String by project
val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
    id("geary.kotlin-conventions")
    id("io.github.slimjar")
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.mineinabyss:idofront:$idofrontVersion")
    implementation("org.reflections:reflections:0.9.12")

    slim("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    slim("com.github.okkero:skedule")
    slim(kotlin("reflect"))

    api(project(":geary-core"))
}

tasks {
    shadowJar {
        archiveBaseName.set("Geary")
    }
}
