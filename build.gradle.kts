import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(idofrontLibs.plugins.kotlin.multiplatform)
    alias(idofrontLibs.plugins.dokka)
    alias(idofrontLibs.plugins.mia.autoversion)
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://jitpack.io")
        mavenLocal()
    }
}

allprojects {
    apply(plugin = "org.jetbrains.dokka")

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        kotlin {
            jvm {
                testRuns["test"].executionTask.configure {
                    useJUnitPlatform()
                }
            }
            js(IR) {
                // TODO JS is missing a Roaring Bitmap implementation, we don't have a use for a JS target
                //  but would happily accept PRs. The target is here to prevent calls to JVM only functions in common
                //  code, ex. JVM only reflection.
                nodejs()
//                browser()
//                disableCompilations()
            }
            sourceSets {
                all {
                    languageSettings {
                        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                        optIn("kotlin.time.ExperimentalTime")
                        optIn("kotlin.ExperimentalUnsignedTypes")
                        optIn("kotlinx.serialization.ExperimentalSerializationApi")
                        optIn("kotlin.RequiresOptIn")
                    }
                }
            }

            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                freeCompilerArgs.addAll(
                    listOf(
                        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                        "-Xexpect-actual-classes"
                    )
                )
            }
        }
    }
}
