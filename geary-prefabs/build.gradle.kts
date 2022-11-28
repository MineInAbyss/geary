plugins {
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
    id("org.jetbrains.dokka")
}
repositories {
}
dependencies {
    compileOnly(libs.fastutil)

    compileOnly(project(":geary-core"))
}
