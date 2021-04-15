pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
    }
}

rootProject.name = "geary"

val useLocalIdofront: Boolean? by settings

if (useLocalIdofront == true){
    includeBuild("../idofront") {
        dependencySubstitution {
            substitute(module("com.mineinabyss:idofront")).with(project(":"))
            substitute(module("com.mineinabyss:idofront-annotation")).with(project(":annotation"))
            substitute(module("com.mineinabyss:idofront-processor")).with(project(":processor"))
        }
    }
}

include("geary-spigot")

project(":geary-spigot").projectDir = file("./spigot")
