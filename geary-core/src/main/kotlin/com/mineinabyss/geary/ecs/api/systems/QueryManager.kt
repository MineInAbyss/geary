package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.GearyEventHandler
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

public object QueryManager {
    private val queries = mutableListOf<Query>()
    private val eventListeners = mutableListOf<GearyListener>()
    private val eventHandlers = mutableListOf<GearyEventHandler>()

    private val archetypes = Component2ObjectArrayMap<Archetype>()

    public fun trackEventListener(listener: GearyListener) {
        eventListeners += listener

        listener::class.nestedClasses
            .mapNotNull {
                // Instantiate child objects or inner classes
                it.takeIf { it.isSubclassOf(GearyEventHandler::class) }?.objectInstance
                    ?: it.takeIf { it.isInner }?.primaryConstructor?.call(listener)
            }.filterIsInstance<GearyEventHandler>()
            .forEach { handler ->
                val handlerMatched = archetypes.match(handler.family)
                handler.parentHolder = listener
                eventHandlers += handler
                for (archetype in handlerMatched) {
                    archetype.addEventHandler(handler)
                }
            }

        val listenerMatched = archetypes.match(listener.family)
        for (archetype in listenerMatched) {
            archetype.addEventListener(listener)
        }
    }

    public fun trackQuery(query: Query) {
        val matched = archetypes.match(query.family)
        query.matchedArchetypes += matched
        queries.add(query)
    }

    internal fun registerArchetype(archetype: Archetype) {
        archetypes.add(archetype, archetype.type)

        val matched = queries.filter { archetype.type in it.family }
        val matchedListeners = eventListeners.filter { archetype.type in it.family }
        val matchedHandlers = eventHandlers.filter { archetype.type in it.family }

        matchedListeners.forEach { archetype.addEventListener(it) }
        matchedHandlers.forEach { archetype.addEventHandler(it) }
        matched.forEach { it.matchedArchetypes += archetype }
    }

    //TODO convert to Sequence
    public fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return archetypes.match(family).flatMap { arc -> arc.ids.map { it.toGeary() } }
    }
}
