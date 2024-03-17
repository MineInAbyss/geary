<div align="center">

# Geary
[![Java CI with Gradle](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/geary-core/maven-metadata.xml&color=light_green)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/geary-core)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/snapshots/com/mineinabyss/geary-core/maven-metadata.xml&label=prerelease)](https://repo.mineinabyss.com/#/snapshots/com/mineinabyss/geary-core)
[![Wiki](https://img.shields.io/badge/-Project%20Wiki-blueviolet?logo=Wikipedia&labelColor=gray)](https://wiki.mineinabyss.com/geary)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://wiki.mineinabyss.com/contribute)
</div>

## Overview

Geary is an Entity Component System (ECS) written in Kotlin. The engine design is inspired by [flecs](https://github.com/SanderMertens/flecs). Core parts of the engine (ex. system iteration, entity creation) are quite optimized, with the main exception being our event system. We use Geary internally for our Minecraft plugins, see [geary-papermc](https://github.com/MineInAbyss/geary-papermc) for more info.

## Features
- Null safe component access
- Flecs-style entity relationships `alice.addRelation<FriendsWith>(bob)`
- Fully type safe system definition
- Prefabs that reuse components across entities
- Persistent components and loading prefabs from files thanks to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/)
- Addon system to use only what you need

## Usage


We host a WIP wiki, the best way to get up-to-date info is reading the [quickstart-guide](https://wiki.mineinabyss.com/geary/guide/quickstart/), here's an excerpt, a simple velocity system:


```kotlin
data class Position(var x: Double, var y: Double)
data class Velocity(var x: Double, var y: Double)

fun GearyModule.updatePositionSystem() = system(object: Query() {
    val position by get<Position>()
    val velocity by get<Velocity>()
}).every(interval = 20.milliseconds).exec {
    // We can access our components like regular variables!
    position.x += velocity.x
    position.y += velocity.y
}


fun main() {
    // Set up geary
    geary(ArchetypeEngineModule) {
        // configure engine here
        install(Prefabs)
    }

    val posSystem = geary.updatePositionSystem()

    // Create an entity the system can run on
    entity {
        setAll(Position(0.0, 0.0), Velocity(1.0, 0.0))
    }

    posSystem.tick() // exec just this system
    geary.engine.tick() // exec all registered repeating systems, interval used to calculate every n ticks to run
    
    val positions: List<Position> = posSystem.map { position }
}

```

### Gradle
```kotlin
repositories {
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    val gearyVersion = "x.y.z"
    implementation("com.mineinabyss:geary-core:$gearyVersion")
    implementation("com.mineinabyss:geary-<addon-name>:$gearyVersion")
}
```

## Roadmap

As the project matures, our primary goal is to make it useful to more people. Here are a handful of features we hope to achieve:
- Multiplatform support, with js, jvm, and native targets
- Publish numbers for benchmarks and cover more parts of the engine with them
- Relation queries, ex. entities with a parent that has X component.
