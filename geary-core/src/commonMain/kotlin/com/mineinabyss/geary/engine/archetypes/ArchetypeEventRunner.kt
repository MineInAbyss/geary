package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.context.archetypes
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EventRunner
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope

class ArchetypeEventRunner : EventRunner {
    private val records: TypeMap get() = archetypes.records

    override fun callEvent(target: Entity, event: Entity, source: Entity?) {
        callEvent(records[target], records[event], source?.let { records[source] })
    }

    fun callEvent(target: Record, event: Record, source: Record?) {
        val origEventArc = event.archetype
        val origTargetArc = target.archetype
        val origSourceArc = source?.archetype

        //TODO performance upgrade will come when we figure out a solution in QueryManager as well.
        for (handler in origEventArc.eventHandlers) {
            // If an event handler has moved the entity to a new archetype, make sure we follow it.
            val (targetArc, targetRow) = target
            val (eventArc, eventRow) = event
            val sourceArc = source?.archetype
            val sourceRow = source?.row

            // If there's no source but the handler needs a source, skip
            if (source == null && !handler.parentListener.source.isEmpty) continue

            // Check that this handler has a listener associated with it.
            if (!handler.parentListener.target.isEmpty && handler.parentListener !in targetArc.targetListeners) continue
            if (sourceArc != null && !handler.parentListener.source.isEmpty && handler.parentListener !in sourceArc.sourceListeners) continue

            // Check that we still match the data if archetype of any involved entities changed.
            if (targetArc != origTargetArc && targetArc.type !in handler.parentListener.target.family) continue
            if (eventArc != origEventArc && eventArc.type !in handler.parentListener.event.family) continue
            if (sourceArc != origSourceArc && eventArc.type !in handler.parentListener.source.family) continue

            val listenerName = handler.parentListener::class.simpleName
            val targetScope = runCatching {
                RawAccessorDataScope(
                    archetype = targetArc,
                    perArchetypeData = handler.parentListener.target.cacheForArchetype(targetArc),
                    row = targetRow,
                )
            }.getOrElse { throw IllegalStateException("Failed while reading target scope on $listenerName", it) }
            val eventScope = runCatching {
                RawAccessorDataScope(
                    archetype = eventArc,
                    perArchetypeData = handler.parentListener.event.cacheForArchetype(eventArc),
                    row = eventRow,
                )
            }.getOrElse { throw IllegalStateException("Failed while reading event scope on $listenerName", it) }
            val sourceScope = if (source == null) null else runCatching {
                RawAccessorDataScope(
                    archetype = sourceArc!!,
                    perArchetypeData = handler.parentListener.source.cacheForArchetype(sourceArc),
                    row = sourceRow!!,
                )
            }.getOrElse { throw IllegalStateException("Failed while reading source scope on $listenerName", it) }
            handler.processAndHandle(sourceScope, targetScope, eventScope)
        }
    }
}
