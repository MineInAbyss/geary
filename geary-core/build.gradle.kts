import Com_mineinabyss_conventions_platform_gradle.Deps
import Geary_kotlin_conventions_gradle.GearyDeps

plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    //ecs-related libs
    implementation(GearyDeps.bimap) { isTransitive = false }
    implementation(GearyDeps.bitvector)
    implementation(Deps.kotlin.reflect) { isTransitive = false }

    //provided by Minecraft
    // TODO implementation here, avoid shading on papermc
    compileOnly(GearyDeps.fastutil)
    testImplementation(GearyDeps.fastutil)
}
