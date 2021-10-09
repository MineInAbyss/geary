import Com_mineinabyss_conventions_platform_gradle.Deps
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val useNMS: String? by project
val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.slimjar")
}

repositories {
    mavenCentral()
}

dependencies {
    slim(kotlin("stdlib-jdk8"))

    slim(Deps.kotlinx.serialization.json)
    slim(Deps.kotlinx.serialization.cbor)
    slim("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.2.1")
    slim(Deps.kotlinx.serialization.kaml) {
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
