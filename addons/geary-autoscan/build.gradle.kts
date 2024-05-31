plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
    id(idofrontLibs.plugins.mia.publication.get().pluginId)
    alias(idofrontLibs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":geary-core"))
    implementation(project(":geary-serialization"))

    implementation(idofrontLibs.reflections)
    implementation(idofrontLibs.kotlin.reflect)
    implementation(idofrontLibs.idofront.di)
}
