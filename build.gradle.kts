import Com_mineinabyss_conventions_platform_gradle.Deps

plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.mineinabyss.conventions.platform")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(Deps.kotlin.stdlib)
}

tasks {
    build {
        dependsOn(project(":geary-platform-papermc").tasks.build)
    }
}
