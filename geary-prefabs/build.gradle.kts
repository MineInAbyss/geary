@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    alias(libs.plugins.mia.publication)
    alias(libs.plugins.mia.testing)
    //id("com.mineinabyss.conventions.publication")
    //id("com.mineinabyss.conventions.testing")
    id("org.jetbrains.dokka")
}
repositories {
}
dependencies {
    compileOnly(libs.fastutil)

    compileOnly(project(":geary-core"))
}
