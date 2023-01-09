@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    id(libs.plugins.mia.publication.get().pluginId)
    id(libs.plugins.mia.testing.get().pluginId)
}

dependencies {
    implementation(libs.reflections)
    implementation(libs.kotlin.reflect)
    implementation(libs.idofront.di)
    implementation(mylibs.kermit)
    compileOnly(libs.idofront.autoscan)
    compileOnly(project(":geary-core"))
    compileOnly(project(":geary-serialization"))
//    compileOnly(project(":geary-prefabs"))
}
