import com.mineinabyss.geary.Deps
import com.mineinabyss.sharedSetup

plugins {
    id("com.github.johnrengelman.shadow")
}

sharedSetup()

subprojects {
    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.codemc.io/repository/nms/")
    }

    dependencies {
        //TODO decide whether we stick with spigot or not since paper adds some nice things
        compileOnly("com.destroystokyo.paper:paper-api:${Deps.serverVersion}")
        compileOnly("com.destroystokyo.paper:paper:${Deps.serverVersion}") // NMS
    }
}

allprojects {
    publishing {
        mineInAbyss(project) {
            from(components["java"])
        }
    }
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    api(project(":geary-spigot:geary-spigot-core"))
    api(project(":geary-spigot:geary-spigot-components"))
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
