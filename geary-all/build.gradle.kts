val includedProjects: String by project


tasks.register("build") {
    for (project in includedProjects.split(',')) {
        println(project)
        dependsOn(gradle.includedBuild(project).task(":build"))
    }
}
