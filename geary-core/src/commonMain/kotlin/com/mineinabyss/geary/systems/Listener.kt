package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.systems.accessors.*
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor

/**
 * #### [Guide: Listeners](https://wiki.mineinabyss.com/geary/guide/listeners)
 *
 * Exposes a way to match against certain combinations of [source]/[target]/[event] entities present on a fired event.
 *
 * [Handler]s can be defined inside by annotating a function with [Handler], these
 * are the actual functions that run when a matching event is found.
 */
abstract class Listener : AccessorOperations(), System {
    val target: AccessorHolder = AccessorHolder()
    val event: AccessorHolder = AccessorHolder()
    val source: AccessorHolder = AccessorHolder()

    fun start() {
        onStart()
    }

    private fun getIndexForHolder(holder: AccessorHolder): Int= when(holder) {
        target -> 0
        event -> 1
        source -> 2
        else -> error("Holder is not a part of this listener: $holder")
    }

    fun <T : ReadOnlyAccessor<A>, A> T.on(holder: AccessorHolder): ReadOnlyEntitySelectingAccessor<T, A> {
        val index = getIndexForHolder(holder)
        if (this is FamilyMatching) this.family?.let { holder.mutableFamily.add(it) }
        return ReadOnlyEntitySelectingAccessor(this, index)
    }

    fun <T : ReadWriteAccessor<A>, A> T.on(holder: AccessorHolder): ReadWriteEntitySelectingAccessor<T, A> {
        val index = getIndexForHolder(holder)
        if (this is FamilyMatching) this.family?.let { holder.mutableFamily.add(it) }
        return ReadWriteEntitySelectingAccessor(this, index)
    }

    /** Fires when an entity has a component of type [T] set or updated. */
    inline fun <reified T: ComponentAccessor<A>, reified A> T.whenSetOnTarget(): ReadWriteEntitySelectingAccessor<T, A> {
        event.mutableFamily.onSet(componentId<A>())
        return this.on(target)
    }

    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
    inline fun <reified T: ComponentAccessor<A>, reified A> T.whenFirstSetOnTarget(): ReadWriteEntitySelectingAccessor<T, A> {
        event.mutableFamily.onFirstSet(componentId<A>())
        return this.on(target)
    }

    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    inline fun <reified T: ComponentAccessor<A>, reified A> T.whenAddedOnTarget(): ReadWriteEntitySelectingAccessor<T, A> {
        event.mutableFamily.onAdd(componentId<A>())
        return this.on(event)
    }

    abstract fun Records.handle()
}

