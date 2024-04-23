package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.events.queries.Observer
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.archetypes

class ObserverList {
    val observersByInvolvedComponent: MutableMap<ComponentId, MutableList<Observer>> = mutableMapOf()

    fun add(observer: Observer) {
        if (observer.involvedComponents.size == 0) {
            observersByInvolvedComponent.getOrPut(0uL) { mutableListOf() }.add(observer)
        } else observer.involvedComponents.forEach { componentId ->
            observersByInvolvedComponent.getOrPut(componentId) { mutableListOf() }.add(observer)
        }
    }

    operator fun get(componentId: ComponentId?): List<Observer> =
        observersByInvolvedComponent[componentId ?: 0uL] ?: emptyList()
}

class ArchetypeEventRunner : EventRunner {
    val observerMap = mutableMapOf<ComponentId, ObserverList>()

    override fun addObserver(observer: Observer) {
        observer.listenToEvents.forEach { event ->
            observerMap.getOrPut(event) { ObserverList() }.add(observer)
        }
    }

    override fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId?,
        entity: Entity,
    ) {
        observerMap[eventType]?.get(involvedComponent)?.forEach { observer ->
            archetypes.records.runOn(entity) { archetype, row ->
                if (observer.family.contains(archetype.type)) {
                    observer.queries.fastForEach { query ->
                        query.apply {
                            originalArchetype = archetype
                            originalRow = row
                            delegated = false
                            cachingAccessors.forEach { it.updateCache(archetype) }
                        }
                    }
                    observer.run(entity, eventData, involvedComponent)
                }
            }
        }
    }
}
