package com.mineinabyss.geary.events

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ULong2ObjectMap
import com.mineinabyss.geary.events.queries.Observer
import com.mineinabyss.geary.helpers.NO_COMPONENT
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.archetypes

class ObserverList {
    val observersByInvolvedComponent = ULong2ObjectMap<MutableList<Observer>>()

    fun add(observer: Observer) {
        if (observer.involvedComponents.size == 0) {
            observersByInvolvedComponent.getOrPut(NO_COMPONENT) { mutableListOf() }.add(observer)
        } else observer.involvedComponents.forEach { componentId ->
            observersByInvolvedComponent.getOrPut(componentId) { mutableListOf() }.add(observer)
        }
    }

    inline fun forEach(componentId: ComponentId, exec: (Observer) -> Unit) {
        observersByInvolvedComponent[NO_COMPONENT]?.fastForEach { exec(it) }
        if (componentId != NO_COMPONENT)
            observersByInvolvedComponent[componentId]?.fastForEach { exec(it) }
    }
}

class ArchetypeEventRunner : EventRunner {
    private val observerMap = ULong2ObjectMap<ObserverList>()

    override fun addObserver(observer: Observer) {
        observer.listenToEvents.forEach { event ->
            observerMap.getOrPut(event) { ObserverList() }.add(observer)
        }
    }

    override fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId,
        entity: Entity,
    ) {
        observerMap[eventType]?.forEach(involvedComponent.withoutRole(HOLDS_DATA)) { observer ->
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
