rootProject.name = "geary-all"
val includedProjects: String by settings

for (project in includedProjects.split(',')) {
    includeBuild("../../$project")
}
