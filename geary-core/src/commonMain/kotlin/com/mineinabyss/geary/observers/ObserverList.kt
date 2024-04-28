package com.mineinabyss.geary.observers

import androidx.collection.LongSparseArray
import androidx.collection.MutableObjectList
import androidx.collection.mutableObjectListOf
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.getOrPut
import com.mineinabyss.geary.helpers.NO_COMPONENT

class ObserverList {
    val involved2Observer = LongSparseArray<MutableObjectList<Observer>>()

    fun add(observer: Observer) {
        if (observer.involvedComponents.size == 0) {
            involved2Observer.getOrPut(0L) { mutableObjectListOf() }.add(observer)
        } else observer.involvedComponents.forEach { componentId ->
            involved2Observer.getOrPut(componentId.toLong()) { mutableObjectListOf() }.add(observer)
        }
    }

    inline fun forEach(componentId: ComponentId, exec: (Observer) -> Unit) {
        involved2Observer[0L]?.forEach(exec)
        if (componentId != NO_COMPONENT)
            involved2Observer[componentId.toLong()]?.forEach(exec)
    }
}
