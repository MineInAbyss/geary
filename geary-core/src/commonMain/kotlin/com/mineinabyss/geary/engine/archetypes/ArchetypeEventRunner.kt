package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.RecordPointer
import com.mineinabyss.geary.datatypes.Records
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

        fun callListener(listener: Listener) {
            val pointers: Records = when (source) {
                null -> Records(RecordPointer(target), RecordPointer(event), null)
                else -> Records(RecordPointer(target), RecordPointer(event), RecordPointer(source))
            }
            with(listener) {
                pointers.handle()
            }
        }

        targetArc.targetListeners.fastForEach {
            if ((it.event.isEmpty || it in eventArc.eventListeners) &&
                (it.source.isEmpty || it in (sourceArc?.sourceListeners ?: emptySet()))
            ) callListener(it)
        }
        eventArc.eventListeners.fastForEach {
            // Check empty target to not double call listeners
            if (it.target.isEmpty &&
                (it.event.isEmpty || it in eventArc.eventListeners) &&
                (it.source.isEmpty || it in (sourceArc?.sourceListeners ?: emptySet()))
            ) callListener(it)
        }
        sourceArc?.sourceListeners?.fastForEach {
            // Likewise both target and event must be empty to not double call listeners
            if (it.target.isEmpty && it.event.isEmpty &&
                (it.target.isEmpty || it in targetArc.targetListeners) &&
                (it.event.isEmpty || it in (eventArc.eventListeners))
            ) callListener(it)
        }
    }
}
