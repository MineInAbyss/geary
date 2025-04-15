# Quickstart

This page gives an overview of Geary's syntax and features, the rest of the guide will dive more in depth and assume less knowledge about ecs. Think of this as a *Geary by example* guide.

## Setup

```kotlin
// Create an isolated engine world
val world: Geary = geary(ArchetypeEngineModule()) {
    // install wanted addons here
}.start()

// Functions can use the world as a receiver
fun Geary.doSomething() {
    entity {
        set(Position(0, 0))
        // ...
    }
}

// Rest of the guide happens in this context
with(world) { 
    doSomething()
}

```

> Most of Geary is prepared for Kotlin multiplatform but currently only the JVM target is fully implemented.

{style="note"}

## Define components

``` kotlin
data class Position(var x: Double, var y: Double)
data class Velocity(var x: Double, var y: Double)
sealed class Alive
```

- Any class can act as a component
- Data classes recommended
- Mutable vars are allowed but won't automatically trigger component changed events unless manually calling `set`
- Use sealed class for 'marker' components that don't have data attached to them.

## Manage entities

```kotlin
// create
val entity = entity()

// remove
entity.removeEntity()

val entity2 = entity {
    ...
} // equivalent to entity().apply { ... }
```

### Set data

```kotlin
val exampleEntity = entity {
    set(Position(0, 0)) // Set Position component
    set(Position(1, 0)) // Override previous Position component with new data
    set<Velocity>(Velocity(1, 1)) // Explicitly define type
    remove<Velocity>() // Unset Velocity component
    add<Alive>() // Add component without data
}
```

### Read data

```kotlin
exampleEntity.apply {
    get<Position>() // return type is Position? (in this case, Position(1, 0))
    get<Alive>() // return type is Alive? (in this case, null)
    has<Alive>() // returns true since we added earlier
    has<Velocity>() // returns false since we removed the component
}
```

## Queries

Queries match entities based on their components, they can be used to read or modify data.

### Creating queries

Queries always extend the `Query` class and filter using delegates.

```kotlin
// A manually defined query matching entities with both Position and Velocity components
class MyQuery: Query() {
    var position by get<Position>()
    val velocity by get<Velocity>()
}

// An anonymous object may be used as a query if we don't need to reference it elsewhere
object: Query() {
    var position by get<Position>()
    val velocity by get<Velocity>()
}
```

#### Shorthands

Geary provides some helper functions to define queries more concisely.

```kotlin
// A shorthand to define read-only queries inline
query<Position, Velocity>()

// These also support nullable types for optional components
query<Position?, Velocity>()
```

### Iterating queries

Queries can be run in-place (ex. when querying for dynamic information like children of an entity) or cached for faster performance.

#### Cached queries

```kotlin
// Cache the query for fast matching
val query = cache(MyQuery())

// get all entities with both position and velocity components
val matchedEntities: List<Entity> = query.entities()

// Extract data from our query
val dataFromMatched: List<Pair<Position, Velocity>> = query.map { position to velocity }

// Run directly on query
query.forEach {
    // we can modify position directly because we declared its delegate as var
    position.x += velocity.x
    position.y += velocity.y
}

// Shorthand queries can be destructured for convenience
cache(query<Position, Velocity>()).forEach { (position, velocity) ->
    println("Position: $position, Velocity: $velocity")
}
```

- Collecting to a list is much slower than iterating directly with forEach since memory needs to be allocated for the list.

#### In-place queries

In-place queries aren't implemented yet, however matching entities can be done using `findEntities`
```kotlin
// Get entities matching a family
findEntities { has<Position>(); has<Velocity>(); }

// Get entities matching a query
findEntities(query<Position, Velocity>())
```

### Ensure block

We can also match arbitrary families, in this case `this` refers to our queried entity, so we use the following syntax:

```kotlin
object: Query() {
    override fun ensure() = this { or { has<Position>(); has<Velocity>() } }
}
```

## Systems

Systems are cached queries with an `exec` block attached, they can also be repeating.

```kotlin
// We define a function to create our system, note the receiver used to have access to the world context.
fun Geary.createVelocitySystem() = system(object: Query() {
    val position by get<Position>()
    val velocity by get<Velocity>()
}).exec {
    position.x += velocity.x
    position.y += velocity.y
}

// Run exec on all entities matching the query
world.createVelocitySystem().tick()
```

### Repeating systems

```kotlin
// We prefer defining systems in functions
fun Geary.createVelocitySystem() = system(object: Query() { ... })
    .every(1.seconds) // Duration used to calculate every n engine ticks the system should exec
    .exec { ... }

world.createVelocitySystem()
world.tick() // ticks all registered repeating systems
```

### Deferred systems

Systems cannot safely or quickly perform entity type modifications (i.e. component add or remove calls, we can only modify already set data, and even this can't call component modify events). Instead, we can iterate over all matched entities, gather data, and then perform modifications once system iteration completes:

```kotlin
fun Geary.createDeferredSystem() = system(object : Query() {
    val string by get<String>()
}).defer {
    // This could be a heavier calculation that benefits from memory being close together!
    string.length > 10
}.onFinish { result: Boolean, entity: GearyEntity ->
    // We can safely do entity modifications here, everything runs in sync!
    if (result) {
        entity.add<TooLong>()
        entity.remove<String>()
    }
}
```

## Observers

Observers listen to events emitted on entities. Geary comes with some built in events for component modifications, like `OnSet`, `OnRemove`, `OnAdd`, `OnEntityRemove`, etc...

### Simple observer

```kotlin
observe<OnEntityRemove>().exec {
    println("Entity removed")
}
val entity = entity()
entity.removeEntity() // Prints "Entity removed"
```

### Filter by involved components

Certain events like `OnSet` involve a component. Observers may check for a single involved component, or any in a list, or not restrict at all.

```kotlin
observe<OnSet>().involving<Position, Velocity>().exec {
    println("Either position or velocity set!")
}
// Equivalent to involving(entityTypeOf(componentId<Position>, componentId<Velocity>()))

val entity = entity()
entity.set(Position(0, 0)) // Prints "Either position or velocity set!"
entity.set(Velocity(0, 0)) // Prints "Either position or velocity set!"
entity.set(Position(1, 1)) // Prints "Either position or velocity set!"
entity.set(UnrelatedComponent()) // Does nothing
```

### Filtering by query

We can also filter to match a query, this is useful for reading data off entities in an event or reacting to several components being set.

```kotlin
observe<OnSet>()
    .involving<Position, Velocity>()
    .exec(query<Position, Velocity>()) { (position, velocity) ->
        println("Position: $position, Velocity: $velocity")
    }
// For short, we can write involving(query<Position, Velocity>()) for simple query definitions.

val entity = entity()
entity.set(Position(0, 0)) // Nothing gets fired since we fail to match query<Position, Velocity>()
entity.set(Velocity(0, 0)) // Prints "Position: Position(0, 0), Velocity: Velocity(0, 0)"
entity.set(Position(1, 1)) // Prints "Position: Position(1, 1), Velocity: Velocity(0, 0)"
```

## Custom Events

Components can be emitted as custom events on entities. These may or may not hold data, and observers can listen to them globally or on a specific entity.

### Event data

Events may or may not hold data, observers can optionally only listen to events with data.

```kotlin
class MyEvent(val data: String)

val entity = entity()

observe<MyEvent>().exec {
    println("Observe without data")
}
observeWithData<MyEvent>().exec {
    println("Observe with data: ${event.data}")
}

entity.emit<MyEvent>() // Prints "Observe without data"
entity.emit(MyEvent("Hello world!")) // Prints "Observe without data" and "Observe with data: Hello world!"
```

### Involved entities

Events can specify an involved component/entity, observers can filter based on this as shown earlier.

```kotlin
entity.emit<MyEvent>(involving = component<Position>())
```

### On a specific entity

Observers can listen to events on a specific entity and its instances.

```kotlin
class OnClick

val button = entity()
val somethingElse = entity()

button.observe<OnClick>().exec {
    println("Button clicked!")
}

button.emit<OnClick>() // Prints "Button clicked!"
somethingElse.emit<OnClick>() // Does nothing
```
