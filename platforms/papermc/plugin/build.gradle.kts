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

    // Other plugins
    compileOnly(mylibs.plugman)
}
