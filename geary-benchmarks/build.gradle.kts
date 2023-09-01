import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.jvm.get().pluginId)
//    id(libs.plugins.mia.publication.get().pluginId)
//    alias(libs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlinx.benchmark") version "0.4.9"
    kotlin("plugin.allopen") version "1.8.20"
}

configure<AllOpenExtension> {
    annotation("org.openjdk.jmh.annotations.State")
}

dependencies {
    implementation(project(":geary-core"))
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.9")
}

benchmark {
    configurations {
        named("main") {
            iterationTime = 5
            iterationTimeUnit = "sec"

        }
    }
    targets {
        register("main") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.21"
        }
    }
}