package com.mineinabyss.geary.observers.entity

import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.observers.EventToObserversMap
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.builders.*

inline fun <reified T : Any> GearyEntity.observe(): ObserverEventsBuilder<ObserverContext> {
    return observe(world.componentId<T>())
}

inline fun <reified T : Any> GearyEntity.observeWithData(): ObserverEventsBuilder<ObserverContextWithData<T>> {
    return observeWithData(world.componentId<T>())
}

/**
 * Attaches an observer to fire on events emitted on this entity and its instances.
 */
fun GearyEntity.attachObserver(observer: Observer) {
    val observerEntity = world.entity {
        // TODO avoid cast
        set(EventToObserversMap(world.records).apply { addObserver(observer) })
        addRelation<ChildOf>(this@attachObserver) // Remove entity when original is removed
    }
    //TODO remove when prefabs auto propagate component adds down
    instances.forEach { it.addRelation<Observer>(observerEntity) }
    addRelation<Observer>(observerEntity)
}

/**
 * Removes an entity observer that was previously attached via [attachObserver]
 */
fun GearyEntity.removeObserver(observer: Observer) = with(world) {
    getRelations<Observer, Any?>().forEach {
        val observerEntity = it.target.toGeary()
        val map = observerEntity.get<EventToObserversMap>()
        if (map != null) {
            map.removeObserver(observer)
            if (map.isEmpty) removeRelation<Observer>(observerEntity)
        }
    }
}

fun GearyEntity.observe(vararg events: ComponentId): ObserverEventsBuilder<ObserverContext> {
    return ObserverWithoutData(events.toList(), world, ::attachObserver)
}


fun <T : Any> GearyEntity.observeWithData(vararg events: ComponentId): ObserverEventsBuilder<ObserverContextWithData<T>> {
    return ObserverWithData(events.toList(), world, ::attachObserver)
}
