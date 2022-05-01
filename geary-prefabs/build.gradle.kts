plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
    id("org.jetbrains.dokka")
}

dependencies {
    compileOnly(gearylibs.fastutil)

    compileOnly(project(":geary-core"))
}
