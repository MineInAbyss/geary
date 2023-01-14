@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.copyjar.get().pluginId)
    id(libs.plugins.mia.kotlin.asProvider().get().pluginId)
    id(libs.plugins.mia.publication.get().pluginId)
    id(libs.plugins.mia.papermc.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

configurations {
    runtimeClasspath {
        fun excludeDep(dep: Provider<MinimalExternalModuleDependency>) {
            val (group, module) = dep.get().module.toString().split(":")
            exclude(group, module)
        }
        excludeDep(libs.fastutil)
        excludeDep(libs.kotlin.reflect)
        excludeDep(libs.kotlinx.coroutines.asProvider())
        excludeDep(libs.reflections)
        exclude("org.jetbrains.kotlinx")
        exclude("org.jetbrains.kotlin")
    }
}

dependencies {
    api(project(":geary-core"))
    api(project(":geary-autoscan"))
    api(project(":geary-prefabs"))
    api(project(":geary-serialization"))
    api(project(":geary-uuid"))
    compileOnly(libs.fastutil)

    // MineInAbyss platform
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.cbor)
    compileOnly(libs.kotlin.reflect) { isTransitive = false }
    compileOnly(libs.reflections)
    compileOnly(libs.minecraft.mccoroutine)

    implementation(libs.idofront.autoscan)
    implementation(libs.bundles.idofront.core)
    implementation(mylibs.kermit)

    // Other plugins
    implementation(mylibs.okio)
    compileOnly(mylibs.plugman)
}
