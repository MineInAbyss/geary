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
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.cbor)

    compileOnly(project(":geary-core"))
    implementation("com.mineinabyss:ding:1.0.0")
}
