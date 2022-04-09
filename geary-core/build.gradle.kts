plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
//    id("geary.kotlin-conventions")
//    id("com.mineinabyss.conventions.publication")
//    id("com.mineinabyss.conventions.testing")
}

buildscript {
    val atomicfuVersion = "0.17.1"

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfuVersion")
    }
}

apply(plugin = "kotlinx-atomicfu")

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js()
    sourceSets {
        all {
            explicitApi()
            languageSettings {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.RequiresOptIn")
            }
        }
        val commonMain by getting {
            dependencies {
                val okioVersion = "3.0.0"
                api("com.squareup.okio:okio:$okioVersion")
                implementation("org.jetbrains.kotlinx:atomicfu:0.17.1")
                implementation("com.benasher44:uuid:0.4.0")
                implementation(libs.kotlin.reflect)
//                implementation(gearylibs.bitvector)
                api(libs.koin.core)// { isTransitive = false }
//                api(libs.kotlinx.coroutines)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                api(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.cbor)
                api("com.soywiz.korlibs.kds:kds:2.2.1")
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
                implementation(libs.kotlinx.serialization.kaml)
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
