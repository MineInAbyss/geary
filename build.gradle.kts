plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

tasks {
    build {
        dependsOn(project(":geary-platform-papermc").tasks.build)
    }
}
