package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.systems.SystemManager
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.entities.children
import com.mineinabyss.idofront.messaging.logError
import net.onedaybeard.bitvector.BitVector
import org.clapper.util.misc.SparseArrayList
import java.util.*
import kotlin.reflect.KClass

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
public open class GearyEngine : TickingEngine() {
    internal val typeMap = mutableMapOf<GearyEntityId, Record>()
    private var currId: GearyEntityId = 0uL

    //TODO there's likely a more performant option
    private val removedEntities = Stack<GearyEntityId>()

    private val classToComponentMap = mutableMapOf<KClass<*>, GearyEntityId>()

    override fun getComponentIdForClass(kClass: KClass<*>): GearyComponentId {
        return classToComponentMap.getOrPut(kClass) {
            entity {
                //TODO add some components for new components here
            }.id
        }
    }

    //TODO Proper pipeline with different stages
    protected val registeredSystems: MutableSet<TickingSystem> = mutableSetOf()

    override fun addSystem(system: TickingSystem): Boolean {
        SystemManager.registerSystem(system)
        return registeredSystems.add(system)
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

    /** Describes how to individually tick each system */
    protected open fun TickingSystem.runSystem() {
        tick()
    }

    override fun scheduleSystemTicking() {
        TODO("Implement a system for ticking independent of spigot")
    }


    @Synchronized
    override fun getNextId(): GearyEntityId = if (removedEntities.isNotEmpty()) removedEntities.pop() else currId++

    override fun getComponentsFor(entity: GearyEntityId): Set<GearyComponent> =
        getRecord(entity)?.run {
            archetype.getComponents(row).toSet()
        } ?: emptySet()


    override fun addComponentFor(entity: GearyEntityId, component: GearyComponentId) {
        getOrAddRecord(entity).apply {
            val record = archetype.addComponent(entity, this, HOLDS_DATA.inv() and component)
            typeMap[entity] = record ?: return
        }
    }

    override fun setComponentFor(entity: GearyEntityId, component: GearyComponentId, data: GearyComponent) {
        getOrAddRecord(entity).apply {
            val record = archetype.setComponent(entity, this, HOLDS_DATA or component, data)
            typeMap[entity] = record ?: return
        }
    }

    override fun removeComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean {
        return getRecord(entity)?.apply {
            val record = archetype.removeComponent(entity, this, component or HOLDS_DATA)
                ?: archetype.removeComponent(entity, this, component)
            typeMap[entity] = record ?: return false
        } != null
    }

    override fun getComponentFor(entity: GearyEntityId, component: GearyComponentId): GearyComponent? =
        getRecord(entity)?.run { archetype[row, component or HOLDS_DATA] }

    override fun hasComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean =
        getRecord(entity)?.archetype?.type?.run {
            //       component  or the version with the HOLDS_DATA bit flipped
            contains(component) || contains(component xor HOLDS_DATA)
        } ?: false

    //TODO might be a smarter way of storing removed entities as an implicit list within a larger list of entities eventually
    override fun removeEntity(entity: GearyEntityId) {
        // remove all children of this entity from the ECS as well
        geary(entity).apply {
            children.forEach { it.removeEntity() }
        }

        getRecord(entity)?.apply {
            archetype.removeEntity(row)
        }
        typeMap.remove(entity)

        //add current id into queue for reuse
        removedEntities.push(entity)
    }

    public override fun getType(entity: GearyEntityId): GearyType = typeMap[entity]?.archetype?.type ?: GearyType()

    override fun setRecord(entity: GearyEntityId, record: Record) {
        typeMap[entity] = record
    }

    private fun getRecord(entity: GearyEntityId) = typeMap[entity]
    private fun getOrAddRecord(entity: GearyEntityId) =
        typeMap.getOrPut(entity, { root.addEntityWithData(entity, listOf()) })
}
