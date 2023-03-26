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
    implementation("org.reflections:reflections:0.10.2")
    implementation(libs.kotlin.reflect)
    implementation(libs.idofront.di)
    implementation(libs.idofront.autoscan)
}
