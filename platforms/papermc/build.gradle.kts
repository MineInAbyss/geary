plugins {
    id("com.mineinabyss.conventions.copyjar")
}

val runNumber: String? = System.getenv("GITHUB_RUN_NUMBER")
if (runNumber != null) version = "$version.$runNumber"

repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases")
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
        excludeDep(libs.kotlinx.coroutines)
        excludeDep(libs.reflections)
        exclude("org.jetbrains.kotlinx")
        exclude("org.jetbrains.kotlin")
    }
}

dependencies {
    // Shaded
    implementation(project(":geary-papermc-plugin"))
}

tasks {
    build.dependsOn(shadowJar)
}
