<div align="center">

# Geary
[![Java CI with Gradle](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/geary-core/maven-metadata.xml)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/geary-core)
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

## Example

A simple ssytem that iterates over all entities with a position and velocity, updating the position every engine tick.
```kotlin
data class Position(var x: Double, var y: Double)
data class Velocity(var x: Double, var y: Double)

class UpdatePositionSystem : TickingSystem(interval = 20.milliseconds) {
    // Specify all components we want (Geary also supports branched AND/OR/NOT statements for selection)
    val Pointer.position by get<Position>()
    val Pointer.velocity by get<Velocity>()

    override fun Pointer.tick() {
        // We can access our components like regular variables!
        position.x += velocity.x
        position.y += velocity.y
    }
}

fun main() {
    // Set up geary
    geary(ArchetypeEngineModule) {
        // configure engine here
        install(Prefabs)
    }

    geary.pipeline.addSystem(UpdatePositionSystem())

    // Create an entity the system can run on
    entity {
        setAll(Position(0.0, 0.0), Velocity(1.0, 0.0))
    }
}

```
## Usage

A WIP wiki can be found at [wiki.mineinabyss.com](https://wiki.mineinabyss.com/geary/)

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
- Component data migrations
- Complex queries (including relations like parent/child)
