plugins {
    java
//    kotlin("multiplatform")
//    id("org.jetbrains.dokka")
}
tasks {
    build {
        dependsOn(project(":geary-platform-papermc").tasks.build)
    }
}
