package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.components.Conditions
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.ecs.components.removeChildren
import com.mineinabyss.geary.ecs.geary
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.zaxxer.sparsebits.SparseBitSet
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf
import org.clapper.util.misc.SparseArrayList
import java.util.*
import kotlin.reflect.KClass

internal typealias ComponentClass = KClass<out GearyComponent>

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
 */
public open class GearyEngine : Engine {

    //TODO function here to schedule events without spigot

    private var currId = 0

    //TODO there's likely a more performant option
    private val removedEntities = Stack<Int>()

    //TODO use archetypes instead
    //TODO system for reusing deleted entities
    protected val registeredSystems: MutableSet<TickingSystem> = mutableSetOf()

    @Synchronized
    override fun getNextId(): Int = if (removedEntities.isNotEmpty()) removedEntities.pop() else ++currId

    override fun addSystem(system: TickingSystem): Boolean = registeredSystems.add(system)

    //TODO get a more memory efficient list, right now this is literally just an ArrayList that auto expands
    private val components = mutableMapOf<ComponentClass, SparseArrayList<GearyComponent>>()
    internal val bitsets = mutableMapOf<ComponentClass, BitVector>()

    override fun getComponentsFor(id: Int): Set<GearyComponent> =
        components.mapNotNullTo(mutableSetOf()) { (_, value) -> value.getOrNull(id) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : GearyComponent> getComponentFor(kClass: KClass<T>, id: Int): T? = runCatching {
        components[kClass]?.get(id) as? T
    }.getOrNull()

    override fun holdsComponentFor(kClass: ComponentClass, id: Int): Boolean = components[kClass]?.get(id) != null

    override fun hasComponentFor(kClass: ComponentClass, id: Int): Boolean = bitsets[kClass]?.contains(id) ?: false
    override fun removeComponentFor(kClass: ComponentClass, id: Int): Boolean {
        val bitset = bitsets[kClass] ?: return false
        if (bitset[id]) {
            bitset[id] = false
            components[kClass]?.set(id, null)
        }
        return true
    }

    override fun <T : GearyComponent> addComponentFor(kClass: KClass<out T>, id: Int, component: T): T {
        components.getOrPut(kClass, { SparseArrayList() })[id] = component
        bitsets.getOrPut(kClass, { bitsOf() }).set(id)
        return component
    }

    override fun enableComponentFor(kClass: ComponentClass, id: Int) {
        if (holdsComponentFor(kClass, id))
            bitsets[kClass]?.set(id, true)
    }

    override fun disableComponentFor(kClass: ComponentClass, id: Int) {
        bitsets[kClass]?.set(id, false)
    }


    override fun getBitsMatching(
        vararg components: ComponentClass,
        andNot: Array<out ComponentClass>,
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
                .mapNotNull { (bitsets[it]) }
                .fold(allowed) { a, b -> a.andNot(b).let { a } }

        if (checkConditions) {
            val conditional = bitsets[Conditions::class] ?: matchingBits
            if (conditional.isEmpty) return matchingBits

            // get all entities in the original bitset with conditional component
            // then set their bit in the original bitset to whether or not its conditions are met
            val conditionComponents = this.components[Conditions::class]
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

internal inline fun SparseBitSet.forEachBit(block: (Int) -> Unit) {
    var i = 0
    while (i >= 0) {
        i = nextSetBit(i + 1)
        block(i)
    }
}


