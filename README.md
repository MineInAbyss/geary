# Geary

### Overview

Geary is a Spigot plugin that adds an Engine for an Entity Component System (ECS). It's currently rather slow (though relative to Minecraft this is insignificant), but it adds some features unique to our situation with Minecraft. 

Geary is also written in and for Kotlin, reducing boilerplate code for end users and internally (by avoiding writing wrappers around existing Java libraries).

We have two Spigot plugins using this ECS to add Minecraft-specific functionality:
- [Mobzy](https://github.com/MineInAbyss/Mobzy) - Custom NMS entities that bridge ECS and Minecraft's very inheritance based design.
- [Looty](https://github.com/MineInAbyss/Looty) - Custom, highly configurable items

## Features

### kotlinx.serialization backend

All components are serialized through kotlinx.serialization, allowing them to be stored in many formats, including JSON, Yaml, and CBOR. 

Both Mobzy and Looty use this to allow for config-based entity creation, and storing components directly in Minecraft's persistent data containers.

## Planned 

### Nice Kotlin syntax

Idiomatic Kotlin syntax for instantiating entities, iterating over them in systems, and creating components.

### Static entity types

A Geary entity can have an entity type whose static components it will inherit. Accessing a component on the entity will return the entity type's component if the entity itself doesn't have one present.

## Limitations
- Lack of automatic data migration for components.
- Speed could be greatly improved with some caching and better engine design.

## Usage

- We have [Github packages](https://github.com/MineInAbyss/Geary/packages) set up for use with gradle/maven, however the API isn't properly maintained yet. Many things will change as the ECS is being built.
- We'll eventually make a wiki explaining how to use things. In the meantime, you can ask questions in `#plugin-dev` on our [Discord](https://discord.gg/QXPCk2y) server.
