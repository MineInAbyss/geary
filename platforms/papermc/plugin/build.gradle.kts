plugins {
    id("geary.kotlin-conventions")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/")
}

dependencies {
    // Shaded
    api(project(":geary-papermc-core"))
    api(project(":geary-web-console"))

    // Other plugins
    compileOnly(gearylibs.plugman)
}