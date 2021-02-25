package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.*
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.*
import com.mineinabyss.geary.ecs.engine.types.GearyTypeMap
import com.mineinabyss.geary.ecs.engine.types.ID_MASK
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
    private val typeMap: GearyTypeMap = GearyTypeMap()
    private val classToComponentMap = mutableMapOf<KClass<*>, GearyEntityId>()

    public fun GearyEntityId.toIntId(): Int = (ID_MASK and this).toInt()

    override fun getComponentIdForClass(kClass: KClass<*>): GearyEntityId {
        return classToComponentMap.getOrPut(kClass) {
            entity {
                //TODO add some components for new components here
            }.id
        }
    }

    //TODO use archetypes instead
    //TODO system for reusing deleted entities
    protected val registeredSystems: MutableSet<TickingSystem> = mutableSetOf()

    override fun addSystem(system: TickingSystem): Boolean = registeredSystems.add(system)

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

    override fun onStart() {
        TODO("Implement a system for ticking independent of spigot")
    }

    private var currId: GearyEntityId = 0uL

    //TODO there's likely a more performant option
    private val removedEntities = Stack<GearyEntityId>()

    @Synchronized
    override fun getNextId(): GearyEntityId = if (removedEntities.isNotEmpty()) removedEntities.pop() else ++currId

    private val components: MutableMap<GearyComponentId, SparseList<GearyComponent>> = mutableMapOf()

    override fun getComponentsFor(id: GearyEntityId): Set<GearyComponent> =
        components.mapNotNullTo(mutableSetOf()) { (_, value) -> value[id.toIntId()] }

    @Suppress("UNCHECKED_CAST")
    override fun <T : GearyComponent> getComponentFor(entity: GearyEntityId, component: GearyComponentId): T? =
        runCatching {
            //TODO this cast will succeed even when it shouldn't because of type erasure with generics
            // consider using reflective typeOf<T>
            components[component]?.get(entity.toIntId()) as? T
        }.getOrNull()

    override fun holdsComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean =
        components[component]?.get(entity.toIntId()) != null

    override fun hasComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean =
        typeMap.has(entity, component)

    override fun <T : GearyComponent> addComponentFor(entity: GearyEntityId, component: T): T {
        val componentId = getComponentIdForClass(component::class)
        typeMap.set(entity, componentId)
        components.getOrPut(componentId, { SparseList() })[entity.toIntId()] = component
        return component
    }

    override fun removeComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean {
        val removed = typeMap.unset(entity, component)
        components[component]?.remove(entity.toIntId())
        return removed
    }

    override fun setFor(entity: GearyEntityId, component: GearyComponentId) {
        typeMap.set(entity, component)
    }

    override fun unsetFor(entity: GearyEntityId, component: GearyComponentId) {
        typeMap.unset(entity, component)
    }

    //TODO might be a smarter way of storing these as an implicit list within a larger list of entities eventually
    override fun removeEntity(entity: GearyEntity) {
        val (entityId) = entity

        //clear relationship with parent and children
        //TODO add option to recursively remove children in the future
        //TODO change when we update parent child relationships
        entity.apply {
            parent = null
        }

        typeMap[entityId].forEach { componentId ->
            components[componentId]!!.remove(entityId.toIntId())
        }
        typeMap.remove(entityId)

        //add current id into queue for reuse
        removedEntities.push(entityId)
    }

    override fun getFamily(
        vararg with: ComponentClass,
        andNot: Array<out ComponentClass>
    ): List<Pair<GearyEntityId, List<Any>>> {
        val list = mutableListOf<Pair<GearyEntityId, List<Any>>>()

        //TODO better access here + try to cache components[componentId()]
        val withComponents = with.map { components[componentId(it)]!! }.sortedBy { it.size }
        val andNotComponents = andNot.map { components[componentId(it)]!! }.sortedByDescending { it.size }

        withComponents.first().apply {
            packed.forEachIndexed forEachEntity@{ packedIndex, component ->
                val entity = unpackedIndices[packedIndex]

                //FIXME the return order here will be wrong since we are sorting by size
                val retrieved = mutableListOf<Any>(component)
                for (i in 1 until withComponents.size) {
                    retrieved.add(withComponents[i][entity] ?: return@forEachEntity)
                }

                //TODO there might be a smarter order to start checking not components to reduce the iteration size greatly
                if (andNotComponents.all { it[entity] == null })
                    //TODO what do we actually use for the index inside our bitsets
                    list.add(entity.toULong() to retrieved)
            }
        }
        return list
    }

    public fun countEntitiesOfType(type: String): Int = TODO()

}
