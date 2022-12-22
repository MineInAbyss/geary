plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("com.mineinabyss.conventions.autoversion")
}

repositories {
    mavenCentral()
}

//tasks {
//    build {
//        dependsOn(project(":geary-papermc").tasks.build)
//    }
//}

subprojects {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    }
}

allprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        kotlin {
//    targets.configureEach {
//        if (name == KotlinMultiplatformPlugin.METADATA_TARGET_NAME) return@configureEach
//        if (platformType != KotlinPlatformType.jvm)
//            disableCompilations()
//    }

            jvm {
                testRuns["test"].executionTask.configure {
                    useJUnitPlatform()
                }
            }
            js {
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
                commonMain {
                    dependencies {
                        implementation("co.touchlab:kermit:1.2.2")
                    }
                }
            }

        }

    }
}
