package com.mineinabyss.geary.observers

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.engine.id
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach

class ArchetypeEventRunner(
    val reader: ArchetypeReadOperations,
    val compProvider: ComponentProvider,
    val records: ArrayTypeMap
) : EventRunner {
    val observerComponent: ComponentId = compProvider.id<Observer>()

    private val eventToObserversMap = EventToObserversMap(records)
    override fun addObserver(observer: Observer) {
        eventToObserversMap.addObserver(observer)
    }

    private inline fun matchObservers(
        eventType: ComponentId,
        involvedComponent: ComponentId,
        entity: EntityId,
        exec: (Observer, Archetype, row: Int) -> Unit
    ) {
        val involved = involvedComponent.withoutRole(HOLDS_DATA)

        // Run entity observers

        records.runOn(entity) { archetype, _ -> archetype.getRelationsByKind(observerComponent) }.forEach { relation ->
            val observerList = reader.get(Relation.of(relation).target, compProvider.id<EventToObserversMap>()) as? EventToObserversMap ?: return@forEach
            observerList[eventType]?.forEach(involved, entity, exec)
        }

        // Run global observers
        eventToObserversMap[eventType]?.forEach(involved, entity, exec)
    }

    override fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId,
        entity: EntityId,
    ) {
        matchObservers(eventType, involvedComponent, entity) { observer, archetype, row ->
            // Observer may change the entity record, so we must get each time.
            if (observer.mustHoldData && eventData == null) return@matchObservers
            if (observer.family.contains(archetype.type)) {
                observer.queries.fastForEach { query ->
                    @OptIn(UnsafeAccessors::class)
                    query.reset(row, archetype)
                }
                observer.handle.run(entity, eventData, involvedComponent)
            }
        }
    }
}
