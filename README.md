<div align="center">

# Geary
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/geary-core/maven-metadata.xml&color=light_green)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/geary-core)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/snapshots/com/mineinabyss/geary-core/maven-metadata.xml&label=prerelease)](https://repo.mineinabyss.com/#/snapshots/com/mineinabyss/geary-core)
[![Wiki](https://img.shields.io/badge/-Project%20Wiki-blueviolet?logo=Wikipedia&labelColor=gray)](https://wiki.mineinabyss.com/geary)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://wiki.mineinabyss.com/contribute)
</div>

## Overview

Geary is an Entity Component System (ECS) written in Kotlin. The engine design is inspired by [flecs](https://github.com/SanderMertens/flecs). Core parts of the engine like system iteration and entity creation are quite optimized, the main exception being our observer system. We use Geary internally for our Minecraft plugins, see [geary-papermc](https://github.com/MineInAbyss/geary-papermc) for more info.

## Features
- Archetype based engine optimized for many entities with similar components
- Type safe systems, queries, and event listeners
- Null safe component access
- Flecs-style entity relationships `alice.addRelation<FriendsWith>(bob)`
- Observers for listening to component changes and custom events
- Prefabs that reuse components across entities
- Persistent components and loading prefabs from files thanks to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/)
- Addon system to use only what you need

## Usage

Read our [Quickstart guide](https://wiki.mineinabyss.com/geary/quickstart/) to see Geary in action, here's an excerpt, a simple velocity system:


```kotlin
data class Position(var x: Double, var y: Double)
data class Velocity(var x: Double, var y: Double)

fun Geary.updatePositionSystem() = system(query<Position, Velocity>())
    .every(interval = 20.milliseconds)
    .exec { (position, velocity) ->
        // We can access our components like regular variables!
        position.x += velocity.x
        position.y += velocity.y
    }


fun main() {
    // Set up geary
    geary(ArchetypeEngineModule) {
        // example engine configuration
        install(Prefabs)
    }

    val posSystem = geary.updatePositionSystem()

    // Create an entity the system can run on
    entity {
        setAll(Position(0.0, 0.0), Velocity(1.0, 0.0))
    }
    
    posSystem.tick() // exec just this system
    geary.engine.tick() // exec all registered repeating systems, interval used to calculate every n ticks to run
    
    val positions: List<Position> = posSystem.map { (pos) -> pos }
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

### Version catalog entries

```toml
[versions]
geary = "x.y.z"

[libraries]
geary-core = { module = "com.mineinabyss:geary-autoscan", version.ref = "geary" }
geary-actions = { module = "com.mineinabyss:geary-actions", version.ref = "geary" }
geary-autoscan = { module = "com.mineinabyss:geary-autoscan", version.ref = "geary" }
geary-prefabs = { module = "com.mineinabyss:geary-prefabs", version.ref = "geary" }
geary-serialization = { module = "com.mineinabyss:geary-serialization", version.ref = "geary" } 
geary-uuid = { module = "com.mineinabyss:geary-uuid", version.ref = "geary" } 
```

## Roadmap

As the project matures, our primary goal is to make it useful to more people. Here are a handful of features we hope to achieve:
- Multiplatform support, with js, jvm, and native targets
- Publish numbers for benchmarks and cover more parts of the engine with them
- Relation queries, ex. entities with a parent that has X component.
