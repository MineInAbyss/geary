<div align="center">

# Geary
[![Java CI with Gradle](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/geary-core/maven-metadata.xml)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/geary-core)
[![Wiki](https://img.shields.io/badge/-Project%20Wiki-blueviolet?logo=Wikipedia&labelColor=gray)](https://wiki.mineinabyss.com/geary)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://wiki.mineinabyss.com/contribute)
</div>

## Overview

<details>
<summary> :warning: Notice: Looking for a new maintainer </summary>

> This project was built from the ground up by myself, but its scope has finally caught up and I wish to move on and leave it in the hands of a bigger team that can get Geary to a state where others outside of Mine in Abyss can use it.\
> \
> If you're interested in building an ECS and find Geary's syntax interesting, I'm currently working on some major cleanup, and documenting usage + backend decisions that will hopefully make it easier to take over or build your own. If you just want a working engine (not using Kotlin), I recommend looking at [flecs](https://github.com/SanderMertens/flecs), otherwise browse through some established Java engines! \
> \
> \- Offz

</details>

Geary is an Entity Component System (ECS) written in Kotlin. The engine design is inspired by [flecs](https://github.com/SanderMertens/flecs). It is currently NOT optimized for performance. We use Geary internally for our Minecraft plugins, see [geary-papermc](https://github.com/MineInAbyss/geary-papermc) for more info.

## Features
- Null safe component access
- Flecs-style entity relationships `alice.addRelation<FriendsWith>(bob)`
- Fully type safe system definition
- Prefabs that reuse components across entities
- Persistent components and loading prefabs from files thanks to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/)
- Addon system to use only what you need

## Example

```kotlin
data class Position(var x: Double, var y: Double)
data class Velocity(var x: Double, var y: Double)

class UpdatePositionSystem : TickingSystem(interval = 20.milliseconds) {
    // Specify all components we want (Geary also supports branched AND/OR/NOT statements for selection)
    val TargetScope.position by get<Position>()
    val TargetScope.velocity by get<Velocity>()

    override fun TargetScope.tick() {
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

    // Systems are queries!
    val positions: List<Position> = UpdatePositionSystem.run {
        filter { it.velocity != Velocity(0.0, 0.0) }
            .map { it.position }
    }
}

```
## Usage

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

### Wiki
A WIP wiki can be found at [wiki.mineinabyss.com](https://wiki.mineinabyss.com/geary/)

## Roadmap

As the project matures, our primary goal is to make it useful to more people. Here are a handful of features we hope to achieve:
- (Ongoing) Multiplatform support, with js, jvm, and native targets
- Optimize key bottlenecks and benchmark the engine
- Component data migrations
- Complex queries (including relations like parent/child)
