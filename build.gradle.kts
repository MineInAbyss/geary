plugins {
    java
    kotlin("jvm") apply false
//    id("org.jetbrains.dokka")
}

tasks {
    build {
        dependsOn(project(":geary-papermc").tasks.build)
    }
}
