plugins {
    `java-library`
    id("org.jetbrains.dokka") version "1.5.30"
}

repositories {
    mavenCentral()
}

tasks {
    build {
        dependsOn(project(":geary-platform-papermc").tasks.build)
    }
}
