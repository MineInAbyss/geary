# Geary

### Overview

Geary is a Spigot plugin that adds an Engine for an Entity Component System (ECS). The engine implementation is currently using bitsets and could be more optimized, especially for iterating systems, though relative to Minecraft's already poor performance this is insignificant. We add some features unique to our situation with Minecraft, like the ability to encode components directly to a Persistent Data Container.

Geary is also written in and for Kotlin, reducing boilerplate code for end users. We also didn't want to make a wrapper around existing Java libraries because of increased code complexity and maintenance efforts.

We have two Spigot plugins using this ECS to add Minecraft-specific functionality:
- [Mobzy](https://github.com/MineInAbyss/Mobzy) - Custom NMS entities that bridge ECS and Minecraft's very inheritance based entities.
- [Looty](https://github.com/MineInAbyss/Looty) - Custom, highly configurable items.

## Features

### kotlinx.serialization backend

All components are serialized through kotlinx.serialization, a reflectionless serialization library which allows us to store them in many formats, including JSON, Yaml, CBOR, and [many more](https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/README.md). Use whatever you prefer!

Both Mobzy and Looty use this to allow for config-based entity creation, and store components directly in Bukkit's Persistent Data Containers using CBOR.

### Nice Kotlin syntax

Idiomatic Kotlin syntax for instantiating entities, iterating over them in systems, and creating components. We generally try to make use of Kotlin specific features like reified types, and we use a DSL to let third party plugins get set up with Geary.

## Planned 

### Prefabs

We plan to build a more extensive prefab system, but currently we have some basic support for this in the form of entity types and static components.

A Geary entity can be created from a GearyEntityType, which will initialize it with new instances of some components, or static components shared between all entities of this type. These static components will persist on the entity but their data won't be written to the entity itself. We avoid unneeded serialization or the need for data migrations in these cases!

### Data migration

We are looking at libraries that provide a DSL for component data migrations. We plan to allow end users to register a list of migrations with Geary and let Geary ensure these migrations get applied when entities get loaded.

## Limitations
- Speed could be greatly improved with some caching and better engine design.
- There is no proper Java support. We are using many Kotlin-specific libraries and want to make this nice to use in Kotlin first. However, we understand parts of this may be useful for Java plugins as well, and plan to expand some support for them in the future (ex you may have to write components in Kotlin but could still write the rest of your plugin as you prefer).
- Generic types on components have an unsafe cast done on them, meaning you should always use star projection. However, the need to use generics on components is likely a sign of inheritance which probably has a better alternative in ECS.

## Usage

- We have [Github packages](https://github.com/MineInAbyss/Geary/packages) set up for use with gradle/maven, however the API isn't properly maintained yet. Many things will change as the ECS is being built.
- We'll eventually make a wiki with API documentation. In the meantime, you can ask questions in `#plugin-dev` on our [Discord](https://discord.gg/QXPCk2y) server.

## Setup and Contributions

Please read our [Setup and Contribution guide](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide).
