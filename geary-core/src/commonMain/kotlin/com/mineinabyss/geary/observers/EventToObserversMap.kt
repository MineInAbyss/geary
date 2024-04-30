package com.mineinabyss.geary.observers

import androidx.collection.LongSparseArray
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.getOrPut

class EventToObserversMap {
    private val eventToObserverMap = LongSparseArray<ObserverList>()

    fun addObserver(observer: Observer) {
        observer.listenToEvents.forEach { event ->
            eventToObserverMap.getOrPut(event.toLong()) { ObserverList() }.add(observer)
        }
    }

    operator fun get(event: ComponentId): ObserverList? = eventToObserverMap[event.toLong()]
}
