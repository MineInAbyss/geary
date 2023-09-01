package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EventRunner
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.systems.Listener

class ArchetypeEventRunner : EventRunner {
    private val records: TypeMap get() = archetypes.records

    override fun callEvent(target: Entity, event: Entity, source: Entity?) {
        callEvent(records[target], records[event], source?.let { records[source] })
    }

    fun callEvent(target: Record, event: Record, source: Record?) {
        val origEventArc = event.archetype
        val origTargetArc = target.archetype
        val origSourceArc = source?.archetype

        // triple intersection of listeners

        val listeners: Set<Listener> = origTargetArc.targetListeners.toMutableSet().apply {
            retainAll(origEventArc.eventListeners)
            retainAll { it.source.isEmpty || (origSourceArc != null && it in origSourceArc.sourceListeners) }
        }
        for (listener in listeners) {
            val pointers: Records = when (source) {
                null -> Records(target, event, null)
                else -> Records(target, event, source)
            }
            with(listener) {
                pointers.handle()
            }
        }
    }
}
