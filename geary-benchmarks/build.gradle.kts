import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension

plugins {
    id(idofrontLibs.plugins.mia.kotlin.jvm.get().pluginId)
//    id(idofrontLibs.plugins.mia.publication.get().pluginId)
//    alias(idofrontLibs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlinx.benchmark") version "0.4.9"
    kotlin("plugin.allopen") version "1.9.10"
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
            exclude("jvmTesting")
            warmups = 3
            iterations = 3
            iterationTime = 5
            iterationTimeUnit = "sec"
        }

        create("fast") {
            exclude("jvmTesting")
            warmups = 1
            iterations = 1
            iterationTime = 3
            iterationTimeUnit = "sec"
        }

        create("fastest") {
            exclude("jvmTesting")
            warmups = 1
            iterations = 1
            iterationTime = 3
            iterationTimeUnit = "sec"
        }

        create("specific") {
//            include("Unpack6Benchmark")
            include("NewEntityBenchmark")
            include("ManyComponentsBenchmark")
            warmups = 1
            iterations = 1
            iterationTime = 3
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
