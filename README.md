[![Java CI with Gradle](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/Geary/actions/workflows/gradle-ci.yml)
[![Package](https://badgen.net/maven/v/metadata-url/repo.mineinabyss.com/releases/com/mineinabyss/geary/maven-metadata.xml)](https://repo.mineinabyss.com/releases/com/mineinabyss/geary)
[![Wiki](https://badgen.net/badge/color/Project%20Wiki/purple?icon=wiki&label)](https://github.com/MineInAbyss/Geary/wiki)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide)


# Geary

### Overview

Geary is an Entity Component System (ECS) written in and for Kotlin designed for Minecraft server plugins. The engine design is inspired by [flecs](https://github.com/SanderMertens/flecs) and uses archetypes for data storage.

We have two Spigot plugins using this ECS to add Minecraft-specific functionality:
- [Mobzy](https://github.com/MineInAbyss/Mobzy) - Custom NMS entities that bridge ECS and Minecraft's very inheritance based entities.
- [Looty](https://github.com/MineInAbyss/Looty) - Custom, highly configurable items.

## Features

### kotlinx.serialization backend

All components are serialized through kotlinx.serialization, a reflectionless serialization library which allows us to store them in many formats, including JSON, Yaml, CBOR, and [many more](https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/README.md). Use whatever you prefer!

Both Mobzy and Looty use this to allow for config-based entity creation, and store components directly in Bukkit's Persistent Data Containers using CBOR. Notably, tile entities and chunks also contain Persistent Data Containers, which we plan to make use of in the future.

### Nice Kotlin syntax

Idiomatic Kotlin syntax for instantiating entities, iterating over them in systems, and creating components. We generally try to make use of Kotlin specific features like reified types, and we use a DSL to let third party addons get set up with Geary.

### Prefabs

Prefabs allow you to reuse components between multiple entities of the same type. The addon DSL allows you to specify a folder to automatically load prefabs from. These may be written in YAML, JSON, and eventually any other backend supported by ktx.serialization.

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
- API changes are still rather common.

## Usage

- We have [Github packages](https://github.com/MineInAbyss/Geary/packages) set up for use with gradle/maven, however the API isn't properly maintained yet. Many things will change as the ECS is being built.
- We'll eventually make a wiki with API documentation. In the meantime, you can ask questions in `#plugin-dev` on our [Discord](https://discord.gg/QXPCk2y) server.

### Gradle
```groovy
repositories {
    maven  { url 'https://repo.mineinabyss.com/releases' }
}

dependencies {
    compileOnly 'com.mineinabyss:geary:<version>'
    // Use the line below if you want both geary and spigot specific stuff
    compileOnly 'com.mineinabyss:geary-spigot:<version>'
}
```

## Contributing
Please read our [Setup and Contribution guide](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide) for more info.
