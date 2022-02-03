plugins {
    id("geary.kotlin-conventions")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
}

//buildscript {
//    val atomicfuVersion = "0.17.0"
//
//    dependencies {
//        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfuVersion")
//    }
//}
//
//apply(plugin = "kotlinx-atomicfu")

dependencies {
    implementation(gearylibs.fastutil)
    implementation(libs.kotlin.reflect) { isTransitive = false }
    api(libs.koin.core) { isTransitive = false }
    api(libs.kotlinx.coroutines)

    //ecs-related libs
    implementation(gearylibs.bimap) { isTransitive = false }
    implementation(gearylibs.bitvector)

    testImplementation(gearylibs.fastutil)
    testImplementation(libs.kotlinx.coroutines)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)
}

repositories {
    mavenCentral()
}
