@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mia.autoversion)
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://jitpack.io")
        mavenLocal()
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
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
                nodejs()
                browser()
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
        }
    }
}
