plugins {
    java
    kotlin("multiplatform") apply false
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

tasks {
    build {
        dependsOn(project(":geary-papermc").tasks.build)
    }
}
