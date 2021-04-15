rootProject.name = "geary-all"
val includedProjects: String by settings

for (project in includedProjects.split(',')) {
    includeBuild("../../$project")
}

//val useLocalIdofront: Boolean? by settings

//if (useLocalIdofront == true){
//    includeBuild("../idofront") {
//        dependencySubstitution {
//            substitute(module("com.mineinabyss:idofront")).with(project(":"))
//            substitute(module("com.mineinabyss:idofront-annotation")).with(project(":annotation"))
//            substitute(module("com.mineinabyss:idofront-processor")).with(project(":processor"))
//        }
//    }
//}
