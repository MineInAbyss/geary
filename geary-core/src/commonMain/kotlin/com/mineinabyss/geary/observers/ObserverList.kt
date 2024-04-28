package com.mineinabyss.geary.observers

import androidx.collection.LongSparseArray
import androidx.collection.MutableObjectList
import androidx.collection.mutableObjectListOf
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.getOrPut
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.NO_COMPONENT
import com.mineinabyss.geary.modules.archetypes

class ObserverList {
    val involved2Observer = LongSparseArray<MutableObjectList<Observer>>()

    fun add(observer: Observer) {
        if (observer.involvedComponents.size == 0) {
            involved2Observer.getOrPut(0L) { mutableObjectListOf() }.add(observer)
        } else observer.involvedComponents.forEach { componentId ->
            involved2Observer.getOrPut(componentId.toLong()) { mutableObjectListOf() }.add(observer)
        }
    }

    inline fun forEach(involvedComp: ComponentId, entity: GearyEntity, exec: (Observer, Archetype, row: Int) -> Unit) {
        val records = archetypes.records

        involved2Observer[0L]?.forEach {
            records.runOn(entity) { archetype, row -> exec(it, archetype, row) }
        }
        if (involvedComp != NO_COMPONENT) involved2Observer[involvedComp.toLong()]?.forEach {
            records.runOn(entity) { archetype, row -> exec(it, archetype, row) }
        }
    }
}
