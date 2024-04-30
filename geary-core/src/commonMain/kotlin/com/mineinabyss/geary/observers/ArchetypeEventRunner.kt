package com.mineinabyss.geary.observers

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary

class ArchetypeEventRunner : EventRunner {
    private val eventToObserversMap = EventToObserversMap()
    override fun addObserver(observer: Observer) {
        eventToObserversMap.addObserver(observer)
    }

    private inline fun matchObservers(
        eventType: ComponentId,
        involvedComponent: ComponentId,
        entity: Entity,
        exec: (Observer, Archetype, row: Int) -> Unit
    ) {
        val observerComp = geary.components.observer
        val records = archetypes.records
        val involved = involvedComponent.withoutRole(HOLDS_DATA)

        // Run entity observers
        records.runOn(entity) { archetype, _ -> archetype.getRelationsByKind(observerComp) }.forEach { relation ->
            val observerList = Relation.of(relation).target.toGeary().get<EventToObserversMap>() ?: return@forEach
            observerList[eventType]?.forEach(involved, entity, exec)
        }

        // Run global observers
        eventToObserversMap[eventType]?.forEach(involved, entity, exec)
    }

    override fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId,
        entity: Entity,
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
