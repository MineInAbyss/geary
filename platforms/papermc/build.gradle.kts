plugins {
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
    maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
    maven("https://jitpack.io")
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
    compileOnly(libs.kotlin.reflect) { isTransitive = false }
    compileOnly(libs.reflections)
    compileOnly(libs.minecraft.mccoroutine)

    implementation(libs.idofront.autoscan)
    implementation(libs.bundles.idofront.core)
    implementation("io.ktor:ktor-server-core:2.2.1")
    implementation("io.ktor:ktor-server-netty:2.2.1")

    // Other plugins
    compileOnly(mylibs.plugman)
}
