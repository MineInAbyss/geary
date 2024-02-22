package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EventRunner
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.systems.Listener

class ArchetypeEventRunner : EventRunner {
    private val records: TypeMap get() = archetypes.records

    override fun callEvent(target: Entity, event: Entity, source: Entity?) {
        callEvent(records[target], records[event], source?.let { records[source] })
    }

    fun callEvent(target: Record, event: Record, source: Record?) {
        val eventArc = event.archetype
        val targetArc = target.archetype
        val sourceArc = source?.archetype

        fun callListener(listener: Listener<*>) {
            val query = listener.query
            //TODO function extract
            query.event.delegated = false
            query.event.originalArchetype = event.archetype
            query.event.originalRow = event.row
            query.delegated = false
            query.originalArchetype = target.archetype
            query.originalRow = target.row
            if (source != null) {
                query.source.delegated = false
                query.source.originalArchetype = source.archetype
                query.source.originalRow = source.row
            }
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
