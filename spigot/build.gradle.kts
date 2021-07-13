import com.mineinabyss.sharedSetup

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
    id("geary.publication-conventions")
}

sharedSetup()

repositories {
    mavenCentral()
//    mineInAbyss()
}

dependencies {
    api(project(":geary-spigot:geary-spigot-core"))
    api(project(":geary-spigot:geary-spigot-components"))
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
