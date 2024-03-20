package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.EventRunner
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.query.QueriedEntity

class ArchetypeEventRunner : EventRunner {
    private val records get() = archetypes.records

    override fun callEvent(target: Entity, event: Entity, source: Entity?) {
        records.runOn(target) { targetArc, targetRow ->
            records.runOn(event) { eventArc, eventRow ->
                if(source != null) records.runOn(source) { sourceArc, sourceRow ->
                    callEvent(targetArc, targetRow, eventArc, eventRow, sourceArc, sourceRow)
                } else callEvent(targetArc, targetRow, eventArc, eventRow, null, null)
            }
        }
    }

    fun callEvent(
        eventArc: Archetype,
        eventRow: Int,
        targetArc: Archetype,
        targetRow: Int,
        sourceArc: Archetype?,
        sourceRow: Int?
    ) {
        fun QueriedEntity.reset(arch: Archetype, row: Int) {
            originalArchetype = arch
            originalRow = row
            delegated = false
        }

        fun callListener(listener: Listener<*>) {
            val query = listener.query
            query.event.reset(eventArc, eventRow)
            query.reset(targetArc, targetRow)
            if(sourceArc != null && sourceRow != null)
                query.source.reset(sourceArc, sourceRow)
            listener.run()
        }

        targetArc.targetListeners.fastForEach {
            if ((it.event.and.isEmpty() || it in eventArc.eventListeners) &&
                (it.source.and.isEmpty() || it in (sourceArc?.sourceListeners ?: emptySet()))
            ) callListener(it)
        }
        eventArc.eventListeners.fastForEach {
            // Check empty target to not double call listeners
            if (it.target.and.isEmpty() &&
                (it.event.and.isEmpty() || it in eventArc.eventListeners) &&
                (it.source.and.isEmpty() || it in (sourceArc?.sourceListeners ?: emptySet()))
            ) callListener(it)
        }
        sourceArc?.sourceListeners?.fastForEach {
            // Likewise both target and event must be empty to not double call listeners
            if (it.target.and.isEmpty() && it.event.and.isEmpty() &&
                (it.target.and.isEmpty() || it in targetArc.targetListeners) &&
                (it.event.and.isEmpty() || it in (eventArc.eventListeners))
            ) callListener(it)
        }
    }
}
