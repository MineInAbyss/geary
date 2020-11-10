# Mobzy

![CI](https://github.com/MineInAbyss/Mobzy/workflows/Java%20CI/badge.svg)

### Overview

Mobzy is a plugin for Spigot/Paper 1.16.2 which allows creating custom entities through an ECS (Entity Component System). It is currently under heavy development.

It also handles injecting custom NMS entity types into the server, and packet interception to allow clients to see them. Some of these more general-use features may be placed into their own library eventually.

### ECS Plans

ECS code currently in this project will eventually be moved into another project, Geary, which will also support custom items.

The project uses kotlinx.serialization for quick and easy serialization of components. This part of the project will eventually be delegated to Story, which will also allow components to be serialized seamlessly to entities.

Currently, the ECS supports defining custom entity types with static components, as well as pathfinder goals (if they have a registered serializable wrapper class). The plan is to eventually implement a behaviour tree system that fits nicely with the ECS to replace pathfinder goals entirely.

Some performance improvements (such as archetypes) may be added for the backend for systems iterating over entities, though compared to the cost of Bukkit's operations and Minecraft's poorly optimized entities, the current system is almost sufficient. 

![Custom Mobs](https://media.discordapp.net/attachments/464678554681081856/625036159772524582/2019-09-21_19.39.27.png?width=1210&height=681)

### Use

- We have [Github packages](https://github.com/MineInAbyss/Mobzy/packages) set up for use with gradle/maven, however the API isn't properly maintained yet. Many things will change as the ECS is being built.
- There currently isn't a wiki explaining how to use things yet. We'll get one done once the plugin is properly released. You can ask about things in #plugin-dev on our [Discord](https://discord.gg/QXPCk2y).

### Additional features

- Custom hitboxes (Minecraft normally lets the client handle that, so it wouldn't work on entity types it doesn't know).
- A small pathfinder goal API, with some premade pathfinders for our own mobs.
- Custom mob spawning system
- Configuration for mob drops and spawn locations