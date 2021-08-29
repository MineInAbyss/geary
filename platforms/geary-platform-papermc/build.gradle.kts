plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
    id("io.github.slimjar")
}

repositories {
    maven("https://jitpack.io")
    maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
}

dependencies {
    implementation("org.reflections:reflections:0.9.12")

    slim("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    slim("com.github.okkero:skedule")
    slim(kotlin("reflect"))
    compileOnly("com.rylinaux:PlugMan:2.2.5")

    api(project(":geary-core"))
}

tasks {
    shadowJar {
        archiveBaseName.set("Geary")
    }
}
