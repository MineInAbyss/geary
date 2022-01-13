import Geary_kotlin_conventions_gradle.GearyDeps

plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    compileOnly(GearyDeps.bimap) { isTransitive = false }
    compileOnly(GearyDeps.fastutil)

    compileOnly(project(":geary-core"))
}
