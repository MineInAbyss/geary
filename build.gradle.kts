plugins {
    java
    kotlin("multiplatform") apply false
    id("org.jetbrains.dokka")
    id("com.mineinabyss.conventions.autoversion")
}

repositories {
    mavenCentral()
}

tasks {
    build {
        dependsOn(project(":geary-papermc").tasks.build)
    }
}
