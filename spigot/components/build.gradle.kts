plugins {
    id("geary.kotlin-conventions")
    id("geary.spigot-conventions")
}

dependencies {
    compileOnly(project(":geary-spigot:geary-spigot-core"))
}
