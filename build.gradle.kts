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

allprojects {
    val runNumber: String? = System.getenv("GITHUB_RUN_NUMBER")
    if (runNumber != null) version = "$version.$runNumber"
}
