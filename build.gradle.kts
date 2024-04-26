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
            // TODO Other targets are missing some implementations like Roaring Bitmaps, but we hope someone
            //  interested can add support in the future. Thus, we force a target that doesn't actually compile
            //  so we can't accidentally use jvm-only code in the common target.
            linuxX64()

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
