import Com_mineinabyss_conventions_platform_gradle.Deps
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    // MineInAbyss platform
    compileOnly("com.okkero:skedule:1.2.8")
    compileOnly(Deps.kotlin.stdlib)
    compileOnly(Deps.kotlinx.serialization.json)
    compileOnly(Deps.kotlinx.serialization.cbor)
    compileOnly(Deps.kotlinx.serialization.hocon)
    compileOnly(Deps.kotlinx.serialization.kaml) {
        exclude(group = "org.jetbrains.kotlin")
    }

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
