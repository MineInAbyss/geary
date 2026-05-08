plugins {
    kotlin("multiplatform")
}

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
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlin.RequiresOptIn")
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xexpect-actual-classes"
        )
    }
}