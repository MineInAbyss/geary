plugins {
    id("geary.publication-conventions")
}

tasks {
    build {
        dependsOn(project(":geary-spigot").tasks.build)
    }
}
