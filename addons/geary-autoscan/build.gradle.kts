@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    compileOnly(project(":geary-core"))
    compileOnly(project(":geary-serialization"))

    //TODO remove from platform and move into mylibs
//    implementation(libs.reflections)
    implementation(libs.reflections)
    implementation(libs.kotlin.reflect)
    implementation(libs.idofront.di)
    implementation(libs.idofront.autoscan)
}
