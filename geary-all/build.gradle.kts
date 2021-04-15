val includedProjects: String by project


tasks.register("build") {
    for (project in includedProjects.split(',')) {
        dependsOn(gradle.includedBuild(project).task(":build"))
    }
}
