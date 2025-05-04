package com.mineinabyss.geary.observers

import androidx.collection.LongSparseArray
import androidx.collection.MutableObjectList
import androidx.collection.mutableObjectListOf
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.getOrPut
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.NO_COMPONENT

class ObserverList(
    val records: ArrayTypeMap,
) {
    val involved2Observer = LongSparseArray<MutableObjectList<Observer>>()

    fun add(observer: Observer) {
        if (observer.involvedComponents.size == 0) {
            involved2Observer.getOrPut(0L) { mutableObjectListOf() }.add(observer)
        } else observer.involvedComponents.forEach { componentId ->
            involved2Observer.getOrPut(componentId.toLong()) { mutableObjectListOf() }.add(observer)
        }
    }

    fun remove(observer: Observer) {
        if (observer.involvedComponents.size == 0) {
            involved2Observer[0L]?.remove(observer)
        } else observer.involvedComponents.forEach { componentId ->
            involved2Observer[componentId.toLong()]?.remove(observer)
        }
    }

    inline fun forEach(involvedComp: ComponentId, entity: EntityId, exec: (Observer, Archetype, row: Int) -> Unit) {
        involved2Observer[0L]?.forEach {
            records.runOn(entity) { archetype, row -> exec(it, archetype, row) }
        }
        if (involvedComp != NO_COMPONENT) involved2Observer[involvedComp.toLong()]?.forEach {
            records.runOn(entity) { archetype, row -> exec(it, archetype, row) }
        }
    }
}
