package com.mineinabyss.geary.observers

import androidx.collection.LongSparseArray
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.getOrPut
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap

class EventToObserversMap(
    val records: ArrayTypeMap,
) {
    private val eventToObserverMap = LongSparseArray<ObserverList>()

    fun addObserver(observer: Observer) {
        observer.listenToEvents.forEach { event ->
            eventToObserverMap.getOrPut(event.toLong()) { ObserverList(records) }.add(observer)
        }
    }

    operator fun get(event: ComponentId): ObserverList? = eventToObserverMap[event.toLong()]
}
