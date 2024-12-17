plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    id(idofrontLibs.plugins.mia.testing.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-core"))
    implementation(project(":geary-serialization"))
    implementation(idofrontLibs.kotlinx.serialization.kaml)

    testImplementation(project(":geary-test"))
    testImplementation(kotlin("test"))
    testImplementation(idofrontLibs.kotlinx.coroutines.test)
    testImplementation(idofrontLibs.kotest.assertions)
    testImplementation(idofrontLibs.kotest.property)
    testImplementation(project(":geary-core"))
    testImplementation(project(":geary-serialization"))
}
