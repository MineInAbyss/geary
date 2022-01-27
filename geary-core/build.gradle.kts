plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

dependencies {
    implementation(gearylibs.fastutil)
    implementation(libs.kotlin.reflect) { isTransitive = false }
    api(libs.koin.core) { isTransitive = false }

    //ecs-related libs
    implementation(gearylibs.bimap) { isTransitive = false }
    implementation(gearylibs.bitvector)
    compileOnly(libs.kotlinx.coroutines)

    testImplementation(gearylibs.fastutil)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)
}
