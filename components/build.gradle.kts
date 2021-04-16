dependencies {
    compileOnly(project(":geary-core"))
}

publishing {
    mineInAbyss(project) {
        from(components["java"])
    }
}
