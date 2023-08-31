package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.systems.accessors.*
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor
import kotlin.properties.ReadOnlyProperty

/**
 * #### [Guide: Listeners](https://wiki.mineinabyss.com/geary/guide/listeners)
 *
 * Exposes a way to match against certain combinations of [source]/[target]/[event] entities present on a fired event.
 *
 * [Handler]s can be defined inside by annotating a function with [Handler], these
 * are the actual functions that run when a matching event is found.
 */
abstract class Listener : AccessorOperations(), System {
    val source: AccessorHolder = AccessorHolder()
    val target: AccessorHolder = AccessorHolder()
    val event: AccessorHolder = AccessorHolder()

    fun start() {
        onStart()
    }

    fun <T : ReadOnlyProperty<Record, A>, A> T.onTarget(): EntitySelectingAccessor<T, A> {
        if (this is FamilyMatching) target.mutableFamily.add(this.family)
        return EntitySelectingAccessor(this, 0)
    }

    fun <T : ReadOnlyProperty<Record, A>, A> T.onEvent(): EntitySelectingAccessor<T, A> {
        if (this is FamilyMatching) event.mutableFamily.add(this.family)
        return EntitySelectingAccessor(this, 1)
    }

    fun <T : ReadOnlyProperty<Record, A>, A> T.onSource(): EntitySelectingAccessor<T, A> {
        if (this is FamilyMatching) source.mutableFamily.add(this.family)
        return EntitySelectingAccessor(this, 2)
    }

    /** Fires when an entity has a component of type [T] set or updated. */
    inline fun <reified T : Component> onSet(): EntitySelectingAccessor<ComponentAccessor<T>, T> {
        event.mutableFamily.onSet(componentId<T>())
        return get<T>().onTarget()
    }

//    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
//    inline fun <reified T : Component> onFirstSet(): AccessorBuilder<ComponentAccessor<T>> {
//        return AccessorBuilder { holder, index ->
//            event.mutableFamily.onFirstSet(componentId<T>())
//            get<T>().build(holder, index)
//        }
//    }

    //TODO support onAdd for relations
    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    inline fun <reified T : Component> onAdd(): Family {
        event.mutableFamily.onAdd(componentId<T>())
        return family { has<T>() }
    }

    abstract fun Records.handle()
}

