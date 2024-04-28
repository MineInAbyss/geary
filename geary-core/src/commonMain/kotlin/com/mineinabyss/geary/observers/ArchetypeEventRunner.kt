package com.mineinabyss.geary.observers

import androidx.collection.LongSparseArray
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.archetypes

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
