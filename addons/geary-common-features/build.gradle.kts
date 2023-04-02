@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
}

dependencies {
    compileOnly(project(":geary-core"))
    compileOnly(project(":geary-prefabs"))

    implementation(libs.idofront.serializers)
}
