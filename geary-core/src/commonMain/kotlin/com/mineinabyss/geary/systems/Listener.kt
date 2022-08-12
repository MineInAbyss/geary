package com.mineinabyss.geary.systems

import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.events.Handler
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.systems.accessors.*
import com.mineinabyss.geary.systems.accessors.types.ComponentAccessor
import com.mineinabyss.geary.systems.accessors.types.DirectAccessor
import kotlin.reflect.KProperty

/**
 * #### [Guide: Listeners](https://wiki.mineinabyss.com/geary/guide/listeners)
 *
 * Exposes a way to match against certain combinations of [source]/[target]/[event] entities present on a fired event.
 *
 * [Handler]s can be defined inside by annotating a function with [Handler], these
 * are the actual functions that run when a matching event is found.
 */
public abstract class Listener : AccessorOperations(), GearySystem, AccessorScopeSelector,
    GearyContext by GearyContextKoin() {
    public val source: AccessorHolder = AccessorHolder()
    public val target: AccessorHolder = AccessorHolder()
    public val event: AccessorHolder = AccessorHolder()

    public fun start() {
        onStart()
    }

    public operator fun <T> Accessor<T>.getValue(thisRef: SourceScope, property: KProperty<*>): T = access(thisRef)
    public operator fun <T> Accessor<T>.getValue(thisRef: TargetScope, property: KProperty<*>): T = access(thisRef)
    public operator fun <T> Accessor<T>.getValue(thisRef: EventScope, property: KProperty<*>): T = access(thisRef)

    public fun <T> AccessorBuilder<ComponentAccessor<T>>.onSource(): ComponentAccessor<T> =
        source.addAccessor { build(source, it) }

    public fun <T> AccessorBuilder<ComponentAccessor<T>>.onTarget(): ComponentAccessor<T> =
        target.addAccessor { build(target, it) }

    public fun <T> AccessorBuilder<ComponentAccessor<T>>.onEvent(): ComponentAccessor<T> =
        event.addAccessor { build(event, it) }

    public fun Family.onSource(): DirectAccessor<Family> =
        source._family.add(this).let { DirectAccessor(this) }

    public fun Family.onTarget(): DirectAccessor<Family> =
        target._family.add(this).let { DirectAccessor(this) }

    public fun Family.onEvent(): DirectAccessor<Family> =
        event._family.add(this).let { DirectAccessor(this) }

    /** Fires when an entity has a component of type [T] set or updated. */
    public inline fun <reified T : Component> onSet(): AccessorBuilder<ComponentAccessor<T>> {
        return AccessorBuilder { holder, index ->
            event._family.onSet(componentId<T>())
            get<T>().build(holder, index)
        }
    }

    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
    public inline fun <reified T : Component> onFirstSet(): AccessorBuilder<ComponentAccessor<T>> {
        return AccessorBuilder { holder, index ->
            event._family.onFirstSet(componentId<T>())
            get<T>().build(holder, index)
        }
    }

    //TODO support onAdd for relations
    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    public inline fun <reified T : Component> onAdd(): Family {
        event._family.onAdd(componentId<T>())
        return family { has<T>() }
    }
}

