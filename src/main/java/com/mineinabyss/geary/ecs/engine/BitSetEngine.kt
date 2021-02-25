/*
package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.*
import com.mineinabyss.geary.ecs.actions.components.Conditions
import com.mineinabyss.geary.ecs.components.ComponentName
import com.mineinabyss.geary.ecs.components.addComponent
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.ecs.components.removeChildren
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.idofront.messaging.logError
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf
import org.clapper.util.misc.SparseArrayList
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

*/
/**
 * The default implementation of Geary's Engine.
 *
 * This engine currently uses a bitset approach for iterating over entities.
 *
 * We hold a map of component classes to arrays (currently using [SparseArrayList], where the index represents an entity.
 *
 * Additionally, we hold a similar map but with bitsets (currently using [BitVector]) which allow us to quickly perform
 * a fold operation to find all the entities that match all of the requested components.
 *
 * There is also support for enabling/disabling components without actually removing them by just toggling a bit
 * in the bitset, but not removing it from the matching array.
 *
 * Lastly there's a very basic implementation for only iterating over components with additional conditions. This is
 * currently quite inefficient, but optional.
 *//*

public open class BitSetEngine : TickingEngine() {
    private val componentNames = mutableMapOf<String, GearyEntityId>()

    override fun getComponentIdForClass(kClass: KClass<*>): GearyEntityId {
        val name = kClass.jvmName
        return componentNames.getOrPut(name) {
            entity {
                addComponent(ComponentName(name))
                //TODO add some components for new components here
            }.gearyId
        }
    }

    //TODO support suspending functions for systems
    // perhaps async support in the future
    override fun tick(currentTick: Long) {
        registeredSystems
            .filter { currentTick % it.interval == 0L }
            .forEach {
                try {
                    it.runSystem()
                } catch (e: Exception) {
                    logError("Error while running system ${it.javaClass.name}")
                    e.printStackTrace()
                }
            }
    }

    */
/** Describes how to individually tick each system *//*

    protected open fun TickingSystem.runSystem() {
        tick()
    }

    override fun onStart() {
        TODO("Implement a system for ticking independent of spigot")
    }

    private var currId: GearyEntityId = 0

    //TODO there's likely a more performant option
    private val removedEntities = Stack<GearyEntityId>()

    //TODO use archetypes instead
    //TODO system for reusing deleted entities
    protected val registeredSystems: MutableSet<TickingSystem> = mutableSetOf()

    @Synchronized
    override fun getNextId(): GearyEntityId = if (removedEntities.isNotEmpty()) removedEntities.pop() else ++currId

    override fun addSystem(system: TickingSystem): Boolean = registeredSystems.add(system)

    //TODO get a more memory efficient list, right now this is literally just an ArrayList that auto expands
    private val components = mutableMapOf<GearyComponentId, SparseArrayList<GearyComponent>>()
    internal val bitsets = mutableMapOf<GearyComponentId, BitVector>()

    override fun getComponentsFor(id: GearyEntityId): Set<GearyComponent> =
        components.mapNotNullTo(mutableSetOf()) { (_, value) -> value.getOrNull(id) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : GearyComponent> getComponentFor(entity: GearyEntityId, component: GearyComponentId): T? =
        runCatching {
            //TODO this cast will succeed even when it shouldn't because of type erasure with generics
            // consider using reflective typeOf<T>
            components[component]?.get(entity) as? T
        }.getOrNull()

    override fun holdsComponentFor(component: GearyComponentId, id: GearyEntityId): Boolean =
        components[component]?.get(id) != null

    override fun hasComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean =
        bitsets[component]?.contains(entity) ?: false

    override fun removeComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean {
        val bitset = bitsets[component] ?: return false
        if (bitset[entity]) {
            bitset[entity] = false
            components[component]?.set(entity, null)
        }
        return true
    }

    override fun <T : GearyComponent> addComponentFor(entity: GearyEntityId, component: T): T {
        val componentId = getComponentIdForClass(component::class)
        components.getOrPut(componentId, { SparseArrayList() })[entity] = component
        bitsets.getOrPut(componentId, { bitsOf() }).set(entity)
        return component
    }

    override fun addEntityFor(id: GearyEntityId, componentId: GearyComponentId) {
        TODO("What do we actually represent entities as within the ECS")
    }

    override fun setFor(entity: GearyEntityId, component: GearyComponentId) {
        if (holdsComponentFor(component, entity))
            bitsets[component]?.set(entity, true)
    }

    override fun unsetFor(entity: GearyEntityId, component: GearyComponentId) {
        bitsets[component]?.set(entity, false)
    }


    public fun getBitsMatching(
        vararg components: GearyComponentId,
        andNot: IntArray,
        checkConditions: Boolean
    ): BitVector {
        //copy one component's bitset and go through the others, keeping only bits present in all sets
        val allowed = components
            //if a set isn't present, there's 0 matches
            .mapTo(mutableListOf()) { (bitsets[it] ?: return bitsOf()) }
            //only copy the first bitset, all further operations only mutate it
            .also { list -> list[0] = list[0].copy() }
            .reduce { a, b -> a.and(b).let { a } }

        //remove any bits present in any bitsets from andNot
        val matchingBits =
            if (andNot.isEmpty())
                allowed
            else andNot
                .map { (bitsets[it]) }
                .filterNotNull()
                .fold(allowed) { a, b -> a.andNot(b).let { a } }

        if (checkConditions) {
            val conditional = bitsets[componentId<Conditions>()] ?: matchingBits
            if (conditional.isEmpty) return matchingBits

            // get all entities in the original bitset with conditional component
            // then set their bit in the original bitset to whether or not its conditions are met
            val conditionComponents = this.components[componentId<Conditions>()]
            matchingBits.copy().apply { and(conditional) }.forEachBit { i ->
                (conditionComponents?.get(i) as? Conditions)?.conditionsMet(components, geary(i))?.let { result ->
                    matchingBits[i] = result
                }
            }
        }
        return matchingBits
    }

    //TODO might be a smarter way of storing these as an implicit list within a larger list of entities eventually
    override fun removeEntity(entity: GearyEntity) {
        val (id) = entity

        //clear relationship with parent and children
        //TODO add option to recursively remove children in the future
        entity.apply {
            parent = null
            removeChildren()
        }

        //clear all components
        bitsets.mapNotNull { (kclass, bitset) ->
            if (bitset[id]) {
                bitset[id] = false
                components[kclass]
            } else
                null
        }.forEach { it[id] = null }

        //add current id into queue for reuse
        removedEntities.push(id)
    }
}
*/
