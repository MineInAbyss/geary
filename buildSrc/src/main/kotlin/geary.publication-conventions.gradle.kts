import com.mineinabyss.sharedSetup

plugins {
    java
    `maven-publish`
    id("org.jetbrains.dokka")
    id("com.mineinabyss.shared-gradle")
}

sharedSetup {
    addGithubRunNumber()
    applyJavaDefaults()
}

publishing {
    mineInAbyss(project) {
        from(components["java"])
    }
}
