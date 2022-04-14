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
    google()
}

kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js {
        browser()
        nodejs()
    }
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
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:atomicfu:0.17.1")
                implementation("com.benasher44:uuid:0.4.0")
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.serialization.cbor)
//                implementation(gearylibs.bitvector)
//                api(libs.kotlinx.coroutines)
                api("com.squareup.okio:okio:3.0.0")
                api(libs.koin.core)// { isTransitive = false }
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                api(libs.kotlinx.serialization.json)
                api("com.soywiz.korlibs.kds:kds:2.2.1")
//                implementation(libs.koin.test.junit5)
                implementation("io.kotest:kotest-assertions-core:5.2.3")
                implementation("io.kotest:kotest-property:5.2.3")
//                implementation("io.kotest:kotest-runner-junit5:5.2.3")
            }

        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.koin.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")

//                implementation("io.insert-koin:koin-test:3.1.5")
//                implementation(libs.koin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.kaml)
                implementation("org.roaringbitmap:RoaringBitmap:0.9.25")
            }
        }
//        val jvmTest by getting {
//            dependencies {
//            }
//        }

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
