<div align="center">

# Geary
[![Java CI with Gradle](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml)
[![Package](https://badgen.net/maven/v/metadata-url/repo.mineinabyss.com/releases/com/mineinabyss/geary-platform-papermc/maven-metadata.xml)](https://repo.mineinabyss.com/releases/com/mineinabyss/geary-platform-papermc)
[![Wiki](https://badgen.net/badge/color/Project%20Wiki/purple?icon=wiki&label)](https://wiki.mineinabyss.com/geary)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://wiki.mineinabyss.com/contribute)
</div>


## Overview

Geary is an Entity Component System (ECS) written in Kotlin and designed for Minecraft server plugins. The engine design is inspired by [flecs](https://github.com/SanderMertens/flecs) and uses archetypes for data storage.

We have two PaperMC plugins using this ECS to add Minecraft-specific functionality:
- [Mobzy](https://github.com/MineInAbyss/Mobzy) - Custom entities that bridge ECS and Minecraft's very inheritance based entities.
- [Looty](https://github.com/MineInAbyss/Looty) - Custom, highly configurable items.

## Features

### kotlinx.serialization backend

All components are serialized through kotlinx.serialization, a reflectionless serialization library which allows us to store them in many formats, including JSON, Yaml, CBOR, and [many more](https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/README.md). Use whatever you prefer!

Both Mobzy and Looty use this to allow for config-based entity creation, and store components directly in Bukkit's Persistent Data Containers using CBOR. Notably, tile entities and chunks also contain Persistent Data Containers, which we plan to make use of in the future.

### Nice Kotlin syntax

Idiomatic Kotlin syntax for instantiating entities, iterating over them in systems, and creating components. We generally try to make use of Kotlin specific features like reified types, and we use a DSL to let third party addons get set up with Geary. See examples below for a preview.

### Prefabs

Prefabs allow you to reuse components between multiple entities of the same type. The addon DSL allows you to specify a folder to automatically load prefabs from. These may be written in YAML, JSON, and eventually any other backend supported by ktx.serialization.

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
Engine.entity {
   setAll(Textures(...), Render(...), Velocity(...))
}
```

And a system that sets a walking animation when entities are moving:

```kotlin
object WalkingAnimationSystem : TickingSystem() {
    // Specify all components we want (Geary also supports branched AND/OR/NOT statements for selection)
    val QueryResult.textures by get<Textures>()
    val QueryResult.render by get<Render>()
    val QueryResult.velocity by get<Velocity>()
    
    override fun QueryResult.tick() {
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

## Planned

### Decoupling from Minecraft

As this project matures, we plan to replace more and more of the Minecraft server with our own code. We hope to do this while maintaining useful features for Bukkit. Our goal is to slowly provide alternatives to some of the more annoying/slow parts of the vanilla server. Currently our plan looks something like this:
- (Ongoing) Ensure core parts of the ECS can operate without Spigot.
- (Ongoing) Avoid interacting with NMS and especially its class heirarchy. Components should work without having to create any custom NMS entity types.
- Support stationary ECS entities that are saved to the chunk and can send the correct packets to players.
- Full-on custom entities with their own AI and pathfinding system.
- ECS chunk backend with some basic block behaviours copied from Minecraft. We would like to achieve:
   - Cubic chunks
   - Better async support
   - Custom ECS blocks

### Data migration

We are looking at libraries that provide a DSL for component data migrations. We plan to allow end users to register a list of migrations with Geary and let Geary ensure these migrations get applied when entities get loaded.

### Queries

A DSL for making in-depth queries to efficiently get exactly the entities you want.

## Limitations
- There is no proper Java support. We are using many Kotlin-specific libraries and want to make this nice to use in Kotlin first. However, we understand parts of this may be useful for Java plugins as well, and plan to expand some support for them in the future (ex you may have to write components in Kotlin but could still write the rest of your plugin as you prefer).
- API changes are still rather common, and the codebase itself needs to be ironed out a lot (there is some unexpected behaviour in many places.)

## Usage

### Gradle
```groovy
repositories {
    maven  { url 'https://repo.mineinabyss.com/releases' }
}

dependencies {
    compileOnly 'com.mineinabyss:geary:<version>'
    // Use the line below if you want to use Geary for your PaperMC plugin
    compileOnly 'com.mineinabyss:geary-platform-papermc:<version>'
}
```

### Wiki
A rudimentary wiki can be found at [wiki.mineinabyss.com](https://wiki.mineinabyss.com/geary/)
