# Listeners

- Listeners use accessors like Queries, but are not iterators.
- Instead, they define `handlers` which run when an event has been called on an entity that matches the requested data.

# Creating a listener

As usual, extend `GearyListener` and add accessors:

```kotlin
class ExplosionBehaviour : GearyListener() {
    private val ResultScope.explosionData by get<Explosion>()
...
```

# Add handlers

Handlers are added by overriding the `GearyHandlerScope.init()` function.

This function gets called on any matched archetype. It contains info regarding that archetype as well as extension functions to make listening to events easier.

You may use `on<EventType>{ event -> ... }` to create a new handler. This handler puts you in a `ResultScope` and passes along an `event: EventType`.

Search for extension functions before using this function, since you may accidentally use events that are not handled in Geary at all (ex regular Bukkit events.)

Here's a potential example from Minecraft:
```kotlin
...

    override fun GearyHandlerScope.init() {
        onItemLeftClick { event ->
            event.player.location.explode(explosionData)
        }
    }
}
```

# Useful events

- `onComponentAdd` - Fires as soon as all requested components are present on an entity.

- `onComponentRemove` - Not yet implemented, but will be similar.

- `ItemActions` - Contains many item-related ones provided by Looty.

- More related with Bukkit events to come...
