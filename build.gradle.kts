@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    java
    alias(libs.plugins.dokka)
    alias(libs.plugins.mia.autoversion)
}

repositories {
    mavenCentral()
}

tasks {
    build {
        dependsOn(project(":geary-papermc").tasks.build)
    }
}
