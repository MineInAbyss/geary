package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.AffectedScope
import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.typeOf

public object QueryManager {
    private val queries = mutableListOf<Query>()
    private val sourceListeners = mutableListOf<GearyListener>()
    private val targetListeners = mutableListOf<GearyListener>()
    private val eventHandlers = mutableListOf<GearyHandler>()

    private val archetypes = Component2ObjectArrayMap<Archetype>()

    init {
        registerArchetype(Engine.rootArchetype)
    }

    public fun trackEventListener(listener: GearyListener) {
        listener::class.functions
            .filter { it.hasAnnotation<Handler>() }
            .map { func ->
                val paramTypes = func.parameters.map { it.type }
                val sourceIndex = paramTypes.indexOf(typeOf<SourceScope>())
                val targetIndex = paramTypes.indexOf(typeOf<AffectedScope>())
                val eventIndex = paramTypes.indexOf(typeOf<EventScope>())
                object : GearyHandler(listener) {
                    override fun handle(source: SourceScope?, target: TargetScope?, event: EventScope?) {
                        val params = arrayOfNulls<Any?>(paramTypes.size)
                        params[0] = this@QueryManager
                        // Pass parameters in the order they need to be, allowing them to be omitted if desired.
                        // handle won't be called if a param is null when it shouldn't be unless misused by end user
                        //TODO throw error on registration when a user misuses it :p
                        if (sourceIndex != -1) params[sourceIndex] = source
                        if (targetIndex != -1) params[targetIndex] = target
                        if (eventIndex != -1) params[eventIndex] = event
                        func.call(*params)
                    }
                }
            }
            .forEach { handler ->
                // Add handlers to any matched event entities
                eventHandlers += handler
                val handlerMatched = archetypes.match(handler.parentListener.event.family)
                for (archetype in handlerMatched) archetype.addEventHandler(handler)
            }

        // Only start tracking a listener for the parts it actually cares for
        if (!listener.source.isEmpty) {
            sourceListeners += listener
            val sourcesMatched = archetypes.match(listener.source.family)
            for (archetype in sourcesMatched) archetype.addSourceListener(listener)
        }
        if (!listener.target.isEmpty) {
            targetListeners += listener
            val targetsMatched = archetypes.match(listener.target.family)
            for (archetype in targetsMatched) archetype.addSourceListener(listener)
        }
        // Match source and target requirements
    }

    public fun trackQuery(query: Query) {
        val matched = archetypes.match(query.family)
        query.matchedArchetypes += matched
        queries.add(query)
    }

    internal fun registerArchetype(archetype: Archetype) {
        archetypes.add(archetype, archetype.type)

        val matched = queries.filter { archetype.type in it.family }
        val matchedSources = sourceListeners.filter { archetype.type in it.source.family }
        val matchedTargets = targetListeners.filter { archetype.type in it.target.family }
        val matchedHandlers = eventHandlers.filter { archetype.type in it.parentListener.event.family }

        matchedSources.forEach { archetype.addSourceListener(it) }
        matchedTargets.forEach { archetype.addTargetListener(it) }
        matchedHandlers.forEach { archetype.addEventHandler(it) }
        matched.forEach { it.matchedArchetypes += archetype }
    }

    //TODO convert to Sequence
    public fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return archetypes.match(family).flatMap { arc -> arc.ids.map { it.toGeary() } }
    }
}
