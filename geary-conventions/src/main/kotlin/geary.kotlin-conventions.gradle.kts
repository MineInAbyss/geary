import Com_mineinabyss_conventions_platform_gradle.Deps
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val useNMS: String? by project
val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
}

repositories {
    mavenCentral()
}

object GearyDeps {
    val bimap = "com.uchuhimo:kotlinx-bimap:1.2"
    val bitvector = "net.onedaybeard.bitvector:bitvector-jvm:0.1.4"
    val fastutil = "it.unimi.dsi:fastutil:8.2.2" //Version on minecraft server
    val reflections = "org.reflections:reflections:0.9.12"
    val plugman = "com.rylinaux:PlugMan:2.2.5"
}

dependencies {
    // MineInAbyss platform
    compileOnly(Deps.kotlin.stdlib)
    compileOnly(Deps.kotlinx.serialization.json)
    compileOnly(Deps.kotlinx.serialization.cbor)
    compileOnly(Deps.kotlinx.serialization.hocon)
    compileOnly(Deps.kotlinx.serialization.kaml) {
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
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
}
