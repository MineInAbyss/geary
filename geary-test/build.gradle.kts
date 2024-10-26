plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-core"))
    implementation(kotlin("test"))
    implementation(idofrontLibs.kotlinx.coroutines.test)
    implementation(libs.koin.test)
    compileOnly(idofrontLibs.junit.jupiter)
}
