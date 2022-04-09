plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
//    id("geary.kotlin-conventions")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
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

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js()
//    targets.all {
//        compilations.all {
//            kotlinOptions {
//                freeCompilerArgs = freeCompilerArgs + listOf(
//                    "-opt-in=kotlin.RequiresOptIn",
//                    "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
//                    "-opt-in=kotlin.time.ExperimentalTime",
//                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
//                    "-Xcontext-receivers",
//                )
//            }
//        }
//    }
    sourceSets {
        all {
            explicitApi()
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.reflect)// { isTransitive = false }
//                implementation(gearylibs.bitvector)
                api(libs.koin.core)// { isTransitive = false }
//                api(libs.kotlinx.coroutines)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                api(libs.kotlinx.serialization.json)
                implementation("com.soywiz.korlibs.kds:kds:2.2.1")
            }

        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                implementation(libs.koin.test)
//                implementation(libs.koin.test.junit5)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.roaringbitmap:RoaringBitmap:0.9.25")
            }
        }

        val jsMain by getting {
            dependencies {
                api(gearylibs.bitvector.js)
            }
        }
    }
}

//dependencies {
//    implementation(gearylibs.fastutil)
//
//    //ecs-related libs
//    implementation(gearylibs.bimap) { isTransitive = false }
//
//    testImplementation(gearylibs.fastutil)
//}
