package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.events.handlers.CheckHandler
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

public class QueryManager(private val engine: Engine) {
    private val queries = mutableListOf<Query>()
    private val sourceListeners = mutableListOf<GearyListener>()
    private val targetListeners = mutableListOf<GearyListener>()
    private val eventHandlers = mutableListOf<GearyHandler>()

    private val archetypes = Component2ObjectArrayMap<Archetype>()

    init {
        registerArchetype(engine.rootArchetype)
    }

    public fun trackEventListener(listener: GearyListener) {
        listener::class.functions
            .filter { it.hasAnnotation<Handler>() }
            .map { func ->
                class FunctionCaller(val kFunction: KFunction<*>, params: List<KClass<*>>) {
                    val types = kFunction.parameters.map { it.type.classifier }

                    init {
                        // First param is the class itself, we only care about function params
                        if (types.drop(1).any { it !in params })
                            error("Event handler on ${listener::class.simpleName} had parameters other than $params")
                    }

                    val indices = params.map { types.indexOf(it) }
                    fun call(vararg args: Any?): Any? {
                        // Pass parameters in the order they need to be, allowing them to be omitted if desired.
                        // handle won't be called if a param is null when it shouldn't be unless misused by end user
                        val argArray = arrayOfNulls<Any?>(types.size)
                        argArray[0] = listener
                        indices.forEachIndexed { i, arrIndex ->
                            if (arrIndex != -1) argArray[arrIndex] = args[i]
                        }
                        kFunction.isAccessible = true
                        return runCatching { kFunction.call(*argArray) }
                            // Don't print the whole reflection error, just the cause
                            .getOrElse { throw it.cause ?: it }
                    }
                }

                val caller = FunctionCaller(func, listOf(SourceScope::class, TargetScope::class, EventScope::class))
                val sourceNullable = typeOf<SourceScope>() !in func.parameters.map { it.type }

                if (func.returnType == typeOf<Boolean>())
                    object : CheckHandler(listener, sourceNullable) {
                        override fun check(source: SourceScope?, target: TargetScope, event: EventScope): Boolean {
                            return caller.call(source, target, event) as Boolean
                        }
                    }
                else
                    object : GearyHandler(listener, sourceNullable) {
                        override fun handle(source: SourceScope?, target: TargetScope, event: EventScope) {
                            caller.call(source, target, event)
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
        query.start()
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
