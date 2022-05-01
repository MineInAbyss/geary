import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()
val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
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

    implementation(libs.idofront.core)
}
