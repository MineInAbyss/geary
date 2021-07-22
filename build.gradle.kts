plugins {
    `java-library`
}

tasks {
    build {
        dependsOn(project(":geary-platform-papermc").tasks.build)
    }
}
