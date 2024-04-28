package com.mineinabyss.geary.observers.entity

import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.ObserverList
import com.mineinabyss.geary.observers.builders.*

inline fun <reified T : Any> GearyEntity.observe(): ObserverEventsBuilder<ObserverContext> {
    return observe(componentId<T>())
}

inline fun <reified T : Any> GearyEntity.observeWithData(): ObserverEventsBuilder<ObserverContextWithData<T>> {
    return observeWithData(componentId<T>())
}

fun GearyEntity.attachObserver(observer: Observer) {
    val observerEntity = entity {
        set(ObserverList().apply { add(observer) })
        addRelation<ChildOf>(this@attachObserver) // Remove entity when original is removed
    }
    addRelation<Observer>(observerEntity)
}

fun GearyEntity.observe(vararg events: ComponentId): ObserverEventsBuilder<ObserverContext> {
    return ObserverWithoutData(events.toList(), geary, ::attachObserver)
}


fun <T : Any> GearyEntity.observeWithData(vararg events: ComponentId): ObserverEventsBuilder<ObserverContextWithData<T>> {
    return ObserverWithData(events.toList(), geary, ::attachObserver)
}
