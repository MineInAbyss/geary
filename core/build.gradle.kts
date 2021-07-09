dependencies {
    //provided by Minecraft
    compileOnly("it.unimi.dsi:fastutil:8.5.4")
    testImplementation("it.unimi.dsi:fastutil:8.5.4")


    //ecs-related libs
    implementation("com.uchuhimo:kotlinx-bimap:1.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("net.onedaybeard.bitvector:bitvector-jvm:0.1.4")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.kotest:kotest-runner-junit5:4.4.2")
    testImplementation("io.kotest:kotest-property:4.4.2")
    testImplementation("io.mockk:mockk:1.10.6")
}

publishing {
    mineInAbyss(project) {
        from(components["java"])
    }
}
