import com.mineinabyss.sharedSetup

sharedSetup {
    processResources()
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://dl.bintray.com/korlibs/korlibs")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.reflections:reflections:0.9.12")
    //TODO I"d like to use kotlinspice here but not sure how to best add dependencies that need to be shaded.
    // For now leave as compile only since this dep is always present and having 2 copies was causing issues.
    compileOnly(kotlin("reflect", version = com.mineinabyss.geary.Deps.kotlinVersion))

    implementation("com.mineinabyss:idofront-nms:0.5.9")
    compileOnly("com.github.okkero:skedule")

    api(project(":geary-core"))
    api(project(":geary-components"))
}
