package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.StaticType
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.ecs.components.removeChildren
import com.mineinabyss.geary.ecs.events.EntityRemovedEvent
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.geary.minecraft.geary
import com.mineinabyss.idofront.events.call
import com.okkero.skedule.schedule
import com.zaxxer.sparsebits.SparseBitSet
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf
import org.bukkit.Bukkit
import org.clapper.util.misc.SparseArrayList
import java.util.*
import kotlin.reflect.KClass

internal typealias ComponentClass = KClass<out GearyComponent>


public class EngineImpl : Engine {
    init {
        //tick all systems every interval ticks
        geary.schedule {
            repeating(1)
            //TODO support suspending functions for systems
            // perhaps async support in the future
            while (true) {
                val currTick = Bukkit.getCurrentTick()
                registeredSystems
                        .filter { currTick % it.interval == 0 }
                        .forEach(TickingSystem::tick)
                yield()
            }
        }
    }

    private var currId = 0

    //TODO there's likely a more performant option
    private val removedEntities = Stack<Int>()

    //TODO use archetypes instead
    //TODO system for reusing deleted entities
    private val registeredSystems = mutableSetOf<TickingSystem>()

    @Synchronized
    override fun getNextId(): Int = if (removedEntities.isNotEmpty()) removedEntities.pop() ?: ++currId else ++currId

    override fun addSystem(system: TickingSystem): Boolean = registeredSystems.add(system)

    //TODO get a more memory efficient list, right now this is literally just an ArrayList that auto expands
    private val components = mutableMapOf<ComponentClass, SparseArrayList<GearyComponent>>()
    internal val bitsets = mutableMapOf<ComponentClass, BitVector>()

    override fun getComponentsFor(id: Int): Set<GearyComponent> = components.mapNotNullTo(mutableSetOf()) { (_, value) -> value.getOrNull(id) }

    override fun getComponentFor(kClass: ComponentClass, id: Int): GearyComponent? = runCatching {
        components[kClass]?.get(id)
    }.getOrNull()

    override fun hasComponentFor(kClass: ComponentClass, id: Int): Boolean = bitsets[kClass]?.contains(id) ?: false
    override fun removeComponentFor(kClass: ComponentClass, id: Int) {
        val bitset = bitsets[kClass] ?: return
        if (bitset[id]) {
            bitset[id] = false
            components[kClass]?.set(id, null)
        }
    }

    override fun <T : GearyComponent> addComponentFor(id: Int, component: T): T {
        components.getOrPut(component::class, { SparseArrayList() })[id] = component
        bitsets.getOrPut(component::class, { bitsOf() }).set(id)
        return component
    }


    override fun getBitsMatching(vararg components: ComponentClass, andNot: Array<out ComponentClass>): BitVector {
        //copy one component's bitset and go through the others, keeping only bits present in all sets
        val allowed = components
                .mapTo(mutableListOf()) { (bitsets[it] ?: return bitsOf()) } //if a set isn't present, there's 0 matches
                .also { list -> list[0] = list[0].copy() } //only copy the first bitset, all further operations only mutate it
                .reduce { a, b -> a.and(b).let { a } }

        //remove any bits present in any bitsets from andNot
        return if (andNot.isEmpty())
            allowed
        else andNot.mapNotNull { (bitsets[it]) }
                .fold(allowed) { a, b -> a.andNot(b).let { a } }
    }

    //TODO might be a smarter way of storing these as an implicit list within a larger list of entities eventually
    override fun removeEntity(entity: GearyEntity) {
        val (id) = entity

        EntityRemovedEvent(entity).call()

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


