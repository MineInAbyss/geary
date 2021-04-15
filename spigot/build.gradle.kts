import com.mineinabyss.miaSharedSetup

plugins {
    java
    idea
    `maven-publish`
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

miaSharedSetup()

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://dl.bintray.com/korlibs/korlibs")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    //TODO decide whether we stick with spigot or not since paper adds some nice things
    compileOnly("com.destroystokyo.paper:paper-api:${Versions.server}")
    compileOnly("com.destroystokyo.paper:paper:${Versions.server}") // NMS
    implementation("org.reflections:reflections:0.9.12")
    //TODO I"d like to use kotlinspice here but not sure how to best add dependencies that need to be shaded.
    // For now leave as compile only since this dep is always present and having 2 copies was causing issues.
    compileOnly(kotlin("reflect", version = Versions.kotlin))

    implementation("com.mineinabyss:idofront-nms:0.5.9")
    compileOnly("com.github.okkero:skedule")

    api(project(":"))
}

tasks {
    shadowJar {
        archiveBaseName.set("Geary")

        dependencies {
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:.*"))
        }

        relocate("com.mineinabyss.idofront", "${project.group}.${project.name}.idofront".toLowerCase())
    }

    build {
        dependsOn(shadowJar)
    }
}

publishing {
    mineInAbyss(project) {
        artifactId = "geary-spigot"
        from(components["java"])
    }
}
