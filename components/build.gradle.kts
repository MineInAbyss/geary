plugins {
    id("geary.kotlin-conventions")
    id("geary.publication-conventions")
}

dependencies {
    compileOnly(project(":geary-core"))
}
