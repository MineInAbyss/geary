val kotlinVersion: String by project
val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://dl.bintray.com/korlibs/korlibs")
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.github.slimjar:slimjar:1.2.4")
    slim("org.reflections:reflections:0.9.12")
    compileOnly(kotlin("reflect", version = kotlinVersion))

    implementation("com.mineinabyss:idofront-nms:$idofrontVersion")
    slim("com.github.okkero:skedule")

    implementation("com.github.MrIvanPlays:JarLoaderAPI:7d5339c8f8")

    api(project(":geary-core"))
}

tasks {
    shadowJar {
        archiveBaseName.set("Geary")

        relocate("com.mineinabyss.idofront", "${project.group}.${project.name}.idofront".toLowerCase())
        relocate("io.github.slimjar.app", "io.github.slimjar.app.${project.group}.${project.name}".toLowerCase())
    }

    build {
        dependsOn(shadowJar)
    }
}
