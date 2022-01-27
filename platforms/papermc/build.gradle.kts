plugins {
    java
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.copyjar")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

configurations {
    runtimeClasspath {
        fun excludeDep(dep: Provider<MinimalExternalModuleDependency>) {
            val (group, module) = dep.get().module.toString().split(":")
            exclude(group, module)
        }
        //TODO any libs here are included in our platform system on papermc, find a way to exclude all automatically
        excludeDep(libs.koin.core)
        excludeDep(libs.fastutil)
        excludeDep(libs.kotlin.reflect)
        excludeDep(libs.reflections)
    }
}

dependencies {
    // Shaded
    implementation(project(":geary-papermc-plugin"))
}

tasks {
    shadowJar {
        archiveBaseName.set("Geary")
    }
}
