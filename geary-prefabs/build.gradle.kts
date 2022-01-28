plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    compileOnly(gearylibs.bimap) { isTransitive = false }
    compileOnly(gearylibs.fastutil)

    compileOnly(project(":geary-core"))
    compileOnly(project(":geary-autoscan"))
}
