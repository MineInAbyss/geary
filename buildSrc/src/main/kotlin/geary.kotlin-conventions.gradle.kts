import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val useNMS: String? by project
val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    slim(kotlin("stdlib-jdk8"))
    slim("org.jetbrains.kotlinx:kotlinx-serialization-json")
    slim("org.jetbrains.kotlinx:kotlinx-serialization-cbor")
    slim("com.charleskorn.kaml:kaml") {
        exclude(group = "org.jetbrains.kotlin")
    }

    if(useNMS.toBoolean())
        implementation("com.mineinabyss:idofront-nms:$idofrontVersion")
    else
        implementation("com.mineinabyss:idofront:$idofrontVersion")
}

kotlin {
    explicitApi()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xinline-classes",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
}
