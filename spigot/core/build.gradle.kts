import com.mineinabyss.sharedSetup

val kotlinVersion: String by project
val idofrontVersion: String by project

plugins {
    id("geary.kotlin-conventions")
    id("geary.spigot-conventions")
}

sharedSetup {
    processResources()
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://dl.bintray.com/korlibs/korlibs")
    maven("https://jitpack.io")
}

dependencies {
    slim("org.reflections:reflections:0.9.12")
    //TODO I"d like to use kotlinspice here but not sure how to best add dependencies that need to be shaded.
    // For now leave as compile only since this dep is always present and having 2 copies was causing issues.
    compileOnly(kotlin("reflect", version = kotlinVersion))

    implementation("com.mineinabyss:idofront-nms:$idofrontVersion")
    slim("com.github.okkero:skedule")

    api(project(":geary-core"))
    api(project(":geary-components"))
}

tasks {
    slimJar {

    }
}
