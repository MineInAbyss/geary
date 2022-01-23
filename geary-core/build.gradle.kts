import Com_mineinabyss_conventions_platform_gradle.Deps

plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    api(libs.koin.core)

    //ecs-related libs
    implementation(gearylibs.bimap) { isTransitive = false }
    implementation(gearylibs.bitvector)
    implementation(Deps.kotlin.reflect) { isTransitive = false }

    //provided by Minecraft
    // TODO implementation here, avoid shading on papermc
    compileOnly(gearylibs.fastutil)
    testImplementation(gearylibs.fastutil)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)
}
