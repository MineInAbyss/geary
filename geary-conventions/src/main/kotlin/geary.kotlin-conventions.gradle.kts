import org.gradle.accessors.dm.LibrariesForLibs
import kotlin.reflect.jvm.internal.impl.resolve.scopes.receivers.ImplicitReceiver

val libs = the<LibrariesForLibs>()

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.mia.kotlin.asProvider().get().pluginId)
}

repositories {
    mavenCentral()
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.minecraft.mccoroutine)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.cbor)
    compileOnly(libs.kotlinx.serialization.hocon)
    compileOnly(libs.kotlinx.serialization.kaml) {
        exclude(group = "org.jetbrains.kotlin")
    }

    implementation(libs.bundles.idofront.core)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
}
