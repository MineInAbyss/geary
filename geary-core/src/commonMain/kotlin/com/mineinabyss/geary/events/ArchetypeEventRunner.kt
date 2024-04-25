package com.mineinabyss.geary.events

import androidx.collection.LongSparseArray
import androidx.collection.MutableObjectList
import androidx.collection.mutableObjectListOf
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.events.queries.Observer
import com.mineinabyss.geary.helpers.NO_COMPONENT
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach
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

    inline fun forEach(componentId: ComponentId, exec: (Observer) -> Unit) {
        involved2Observer[0L]?.forEach(exec)
        if (componentId != NO_COMPONENT)
            involved2Observer[componentId.toLong()]?.forEach(exec)
    }
}

class ArchetypeEventRunner : EventRunner {
    private val observerMap = LongSparseArray<ObserverList>()

    override fun addObserver(observer: Observer) {
        observer.listenToEvents.forEach { event ->
            observerMap.getOrPut(event.toLong()) { ObserverList() }.add(observer)
        }
    }

    override fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId,
        entity: Entity,
    ) {
        val archetypes = archetypes
        observerMap[eventType.toLong()]?.forEach(involvedComponent.withoutRole(HOLDS_DATA)) { observer ->
            // Observer may change the entity record, so we must get each time.
            archetypes.records.runOn(entity) { archetype, row ->
                if (observer.mustHoldData && eventData == null) return@runOn
                if (observer.family.contains(archetype.type)) {
                    observer.queries.fastForEach { query ->
                        @OptIn(UnsafeAccessors::class)
                        query.reset(row, archetype)
                    }
                    observer.run(entity, eventData, involvedComponent)
                }
            }
        }
    }
}
