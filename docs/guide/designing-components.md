# Designing Components

## Use [`data`](https://kotlinlang.org/docs/data-classes.html) classes when possible

Data classes auto-generate useful methods like `copy`, `equals`, and `hashCode`. Try to use them for all your components, ex:

```kotlin
data class Location(val x: Int, val y: Int, val z: Int)
```

## Use [`sealed`](https://kotlinlang.org/docs/sealed-classes.html) classes for data-less components

If a component should only ever be added to an entity, not set, a sealed class ensures it cannot be instantiated and nobody else can extend it, ex:

```kotlin
sealed class ReadyForBattle

entity.add<ReadyForBattle>()
entity.set(ReadyForBattle()) // Fails to compile
```

## Avoid inheritance

Inheritance breaks modularity. Only use it if a library you work with already uses it.

```kotlin
class Animal { ... }
class Pig: Animal { ... }

entity().set(Pig())
```
**Unclear whether this should be set as an animal or pig.**
If we set a `Pig`, then anything that reads `Animal` will think it's not there! Similarly, if we set `Animal`, then nothing will know it's actually a `Pig`. Finally, if our hierarchy is big enough, we can't reasonably set every possibility.
{.error}

**If absolutely necessary,**
set the component both as a reasonable common parent class and as the exact class. For instance, in Minecraft we use the common `Entity` class and the specific mob class.
{.warning}

```kotlin
sealed class Alive
data class Oinks(interval: Duration)

entity {
    add<Alive>()
    set(Oinks(5.seconds))
}
```

**Each component does one thing clearly.**
We can access either component without worrying about the other, or both if we like to.
{.success}

## Aim for immutable components

Keep component properties immutable (`val`) unless they are absolutely performance critical.

```kotlin
data class Health(var amount: Int)

val myHealth = Health(20)
entity {
    set(myHealth)
}
myHealth.amount = 0
```
We can change amount without anyone knowing!
{.error}

```kotlin
data class Health(val amount: Int)

entity {
    set(Health(20))
    set(Health(0))
}
```

Changing health must be done through Geary, it can notify any listeners!
{.success}

## Extra

### Don't set generic types like `#!kotlin List<String>`

It is highly discouraged to use generic types with your components, because we can't actually verify those types when getting a component. This is called type erasure. For example:

```kotlin
entity {
    set(listOf("strings")) // sets a list of strings as a component
    get<List<Int>>() // will succeed!
}
```

`#!kotlin get<List<Int>>()` succeeds because there is no way for us to know the generic type of the list during runtime. However, an error
will be thrown when trying to access elements of the list which thought were integers, but are actually strings.
