plugins {
    id("geary.kotlin-conventions")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.copyjar")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.koin.core)

    // Shaded
    implementation(project(":geary-platform-papermc"))
    implementation(project(":geary-web-console"))

    // Other plugins
    compileOnly(gearylibs.plugman)
}
tasks {
    shadowJar {
        archiveBaseName.set("Geary")
    }
}
