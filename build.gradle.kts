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

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    }
}
