# Entities

This guide is currently incomplete
A lot more info will be added soon to give a proper overview needed to get started with Geary.
{.warning}

## Definitions

|**Definition:** Entity
|
||An entity is a unique *thing* that holds information.

{.info}

Notice how broad this definition is. An entity could be a zombie, a place in the world, the sound made by a player's footstep. What makes our entity unique is an `identifier` that represents it, in our case a 64-bit number we call `EntityId`.

|**Definition:** Component
|
||Individual pieces of data on an entitiy are called components.
||For instance, a component could be a `Location`, the `Sprite` of a monster, the `Health` of a player.

{.info}

These are not inherently related to each other, for instance an entity can have a location but no sprite if it is invisible. However, components are very useful together. With a sprite and location, we can render something on screen!

**Tip:** Each component should hold specific data, i.e. *be good at one thing*. This lets us choose exactly the data we need to write clean, modular code.
{.tip}

## Syntax

Let's have a look at creating entities and giving them components.

```kotlin
// Create an empty entity
val entity: Entity = entity()

entity.id // Get the unique id of this entity

// Define a Location component (2)
class Location(val x: Double, val y: Double, val z: Double)

// Set(1) a location to our created entity
entity.set(Location(0.0, 10.0, 0.0))

// Do both at the same time (3)
val anotherEntity = entity {
    set(Location(0.0, 0.0, 0.0))
}

// Read the data we set
entity.get<Location>() // returns Location?
```

1. The `set` operation gives a component to our entity. We'll learn more about it shortly.
2. Notice this is just a regular class! We can add any class as a component, even if we didn't make it ourselves.
3. This is the same as doing `#!kotlin entity().apply { ... }`

|**Why make an Entity class?**
|
||Notice how calling `entity()` returned `Entity` instead of a number. While it is useful to represent entities as numbers internally, if we wrote an operation like `set` for all numbers, someone could accidentally do `#!kotlin 42.set(...)` without ever making a 42nd entity!
||
||So, we use `Entity` to write safer code and keep all the operations together. Click on the title below to see full documentation.

{.info}

**Typealiases.**
Geary provides typealiases like `GearyEntity` for most classes so you can avoid conflicts if you already have a class named `Entity`.
{.tip}


## [`Entity`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/index.html) operations

### [`set`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/set.html) gives an entity a component with data

```kotlin
entity.set(SomeData())
```

`set` takes a type parameter as a key. This is useful when working with another object-oriented application. For example, in Minecraft `Player` extends `LivingEntity`. We can set the player as a `LivingEntity` like so:

```kotlin
entity.set<LivingEntity>(player)
```


### [`get`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/get.html) reads a component of the given type

```kotlin
val data = entity.get<SomeData>() // returns SomeData? (1)
```

1. Notice, the returned type is nullable. We need to handle the case when our entity doesn't have this component set.

### [`add`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/add.html) assigns a component type to an entity, without attaching data

```kotlin
entity.add<Alive>()
```

`add` is useful for marker components that don't need to store data. Later we will explore how this feature lets us create relations to other entities.

|**Can we `add` and `set` the same component?**
||Yes, all set components are also added ones. Adding again will not remove any data.

{.info}


### [`has`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/has.html) checks whether an entity has a set/added component

```kotlin
entity.has<SomeComponent>() // returns Boolean
```

### [`remove`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.datatypes/-entity/remove.html) removes a set/added component of a given type

```kotlin
entity.remove<SomeComponent>()
```

### [`with`](https://mineinabyss.com/Geary/geary-core/com.mineinabyss.geary.helpers/with.html) runs code if an entity has all requested components set

```kotlin
entity.with { loc: Location, sprite: Sprite, health: Health ->
    println("I have all of $loc, $sprite, and $health on me!")
}
```

## Null safety

Kotlin's [null safety](https://kotlinlang.org/docs/null-safety.html) is extremely handy when trying to access components, because we are usually not aware of all the components an entity *could* have.

Null safety ensures we know what to do when a component isn't present. Here are some common use cases:

```kotlin
entity.get<A>() ?: return // (1)
entity.get<B>() ?: B() // (2)
entity.getOrSet<C> { C() } // (3)
```

1. Tries to get `A` or stops if not present.
2. Tries to get `B` or uses a default value.
3. Tries to get `C` or sets and returns a default value.
