@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("geary.kotlin-conventions")
    alias(libs.plugins.mia.papermc)
    alias(libs.plugins.kotlinx.serialization)
}

repositories {
    maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
}

dependencies {
    // Shaded
    api(project(":geary-papermc-core"))

    // Other plugins
    compileOnly(mylibs.plugman)
}
