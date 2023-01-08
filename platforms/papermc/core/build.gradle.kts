@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("geary.kotlin-conventions")
    alias(libs.plugins.dokka)
    alias(libs.plugins.mia.publication)
    alias(libs.plugins.mia.papermc)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.idofront.autoscan)
    api(project(":geary-addon"))
    api(project(":geary-core"))
    api(project(":geary-prefabs"))

    // MineInAbyss platform
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.kotlin.reflect) { isTransitive = false }
    compileOnly(libs.reflections)
}
