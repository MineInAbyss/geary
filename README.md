<div align="center">

# Geary
[![Java CI with Gradle](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/geary-papermc-core/maven-metadata.xml)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/geary-papermc-core)
[![Wiki](https://img.shields.io/badge/-Project%20Wiki-blueviolet?logo=Wikipedia&labelColor=gray)](https://wiki.mineinabyss.com/geary)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://wiki.mineinabyss.com/contribute)
</div>

> **:warning: Notice: Looking for a new maintainer :)**\
> This project was built from the ground up by myself, but its scope has finally caught up and I wish to move on and leave it in the hands of a bigger team that can get Geary to a state where others outside of Mine in Abyss can use it.\
> \
> If you're interested in building an ECS and find Geary's syntax interesting, I'm currently working on some major cleanup, and documenting usage + backend decisions that will hopefully make it easier to take over or build your own. If you just want a working engine (not using Kotlin), I recommend looking at [flecs](https://github.com/SanderMertens/flecs), otherwise browse through some established Java engines! \
> \
> \- Offz

## Overview

Geary is an Entity Component System (ECS) written in Kotlin and designed for Minecraft server plugins. The engine design is inspired by [flecs](https://github.com/SanderMertens/flecs) and uses archetypes for data storage.

## Features

### kotlinx.serialization backend

All components are serialized through kotlinx.serialization, a reflectionless serialization library which allows us to store them in many formats. Use whatever you prefer!

Both Mobzy and Looty use this to allow for config-based entity creation, and store components directly in Bukkit's Persistent Data Containers using a binary format. Notably, tile entities and chunks also contain Persistent Data Containers, which we plan to make use of in the future.

### Nice Kotlin syntax

Idiomatic Kotlin syntax for instantiating entities, iterating over them in systems, and creating components. We generally try to make use of Kotlin specific features like reified types, and we use a DSL to let third party addons get set up with Geary. See examples below for a preview.

### Prefabs

Prefabs allow you to reuse components between multiple entities of the same type. The addon DSL allows you to specify a folder to automatically load prefabs from. These may be written in YAML, JSON, and eventually any other backend supported by ktx.serialization.

### Plugins using geary

Consider having a look at our other projects currently using Geary.

- [Mobzy](https://github.com/MineInAbyss/Mobzy) - Custom entities that bridge ECS and Minecraft's very inheritance based entities
- [Looty](https://github.com/MineInAbyss/Looty) - Custom, highly configurable items
- [Blocky](https://github.com/MineInAbyss/Blocky) - Custom blocks, furniture and more
- [Chatty](https://github.com/MineInAbyss/Chatty) - Customizes chat messages with MiniMessage support

## Examples

Start with some sample components (note we can make them persistent by adding some annotations):

```kotlin
class Texture { ... }

class Textures(
    val idle: Texture,
    val walking: Texture
)

class Render(var activeTexture: Texture)

class Velocity(...) {
    fun isNotZero(): Boolean
}
```

An entity to get us started:

```kotlin
entity {
   setAll(Textures(...), Render(...), Velocity(...))
}
```

And a system that sets a walking animation when entities are moving:

```kotlin
object WalkingAnimationSystem : TickingSystem() {
    // Specify all components we want (Geary also supports branched AND/OR/NOT statements for selection)
    val TargetScope.textures by get<Textures>()
    val TargetScope.render by get<Render>()
    val TargetScope.velocity by get<Velocity>()
    
    override fun TargetScope.tick() {
      // We can access our components like regular variables!
      render.activeTexture = when(velocity.isNotZero()) {
           true -> textures.walking
           false -> textures.idle
      }
   }
}
```

A nifty feature: systems are just queries, which are iterators!

```kotlin
WalkingAnimationSystem.apply {
   filter { it.velocity.isNotZero() }
      .map { it.render.activeTexture }
} // Returns a list of textures for any moving entity
```

## Usage

### Gradle
```groovy
repositories {
    maven  { url 'https://repo.mineinabyss.com/releases' }
}

dependencies {
    compileOnly 'com.mineinabyss:geary:<version>'
    // Use the line below if you want to use Geary for your PaperMC plugin
    compileOnly 'com.mineinabyss:geary-papermc-core:<version>'
}
```

### Wiki
A rudimentary wiki can be found at [wiki.mineinabyss.com](https://wiki.mineinabyss.com/geary/)

## Roadmap

As the project matures, our primary goal is to make it useful to more people. Here are a handful of features we hope to achieve:
- (Ongoing) Multiplatform support, with js, jvm, and native targets
- (Ongoing) Monitoring tools, such as a web GUI
- Optimize key bottlenecks and benchmark the engine
- Support multiple Minecraft platforms (ex Sponge, Fabric)
- Component data migrations
- Complex queries
- Minecraft entities done entirely through packets, with better AI support and faster iteration

## Limitations
- API changes are still rather common, and the codebase itself needs to be ironed out a lot (there is unexpected behaviour in many places.)
- The current archetype architecture leaves many empty archetypes over time which hurts memory usage and performance.
