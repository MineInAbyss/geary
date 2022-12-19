plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
}

dependencies {
    // Shaded
    api(project(":geary-papermc-core"))
    implementation(libs.bundles.idofront.core)
    implementation("io.ktor:ktor-server-core:2.2.1")
    implementation("io.ktor:ktor-server-netty:2.2.1")

    // Other plugins
    compileOnly(mylibs.plugman)
}
