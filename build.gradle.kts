plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

tasks {
    build {
        dependsOn(project(":geary-platform-papermc").tasks.build)
    }
}
