val serverVersion: String by project

plugins {
    java
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.io/repository/nms/")
}

dependencies {
    //TODO decide whether we stick with spigot or not since paper adds some nice things
    compileOnly("com.destroystokyo.paper:paper-api:$serverVersion")
    compileOnly("com.destroystokyo.paper:paper:$serverVersion") // NMS
}
