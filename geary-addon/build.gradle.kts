@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("geary.kotlin-conventions")
    alias(libs.plugins.kotlinx.serialization)
    id(libs.plugins.mia.publication.get().pluginId)
    id(libs.plugins.mia.testing.get().pluginId)
    //id("com.mineinabyss.conventions.publication")
    //id("com.mineinabyss.conventions.testing")
}

dependencies {
    implementation(libs.reflections)
    implementation(libs.kotlin.reflect)
    compileOnly(libs.idofront.autoscan)
    compileOnly(project(":geary-core"))
    compileOnly(project(":geary-prefabs"))
}
