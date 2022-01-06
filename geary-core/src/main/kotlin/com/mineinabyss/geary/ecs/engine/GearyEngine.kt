package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.temporaryEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.entities.children
import com.mineinabyss.geary.ecs.events.AddedComponent
import com.mineinabyss.idofront.messaging.logError
import kotlin.reflect.KClass

/**
 * The default implementation of Geary's Engine.
 *
 * This engine uses [Archetype]s. Each component is an entity itself with an id associated with it.
 * We keep track of each entity's components in the form of it's [GearyType] stored in the [typeMap].
 *
 * Learn more [here](https://github.com/MineInAbyss/Geary/wiki/Basic-ECS-engine-architecture).
 */
public open class GearyEngine : TickingEngine() {
    private val typeMap = TypeMap()
    private var currId: GearyEntityId = 0uL

    final override val rootArchetype: Archetype = Archetype(GearyType(), 0)
    private val archetypes = mutableListOf(rootArchetype)
    private val removedEntities = EntityStack()
    private val classToComponentMap = mutableMapOf<KClass<*>, GearyEntityId>()

    init {
        //Register an entity for the ComponentInfo component, otherwise getComponentIdForClass does a StackOverflow
        val componentInfo = entity()
        classToComponentMap[ComponentInfo::class] = componentInfo.id
//        componentInfo.set(ComponentInfo(ComponentInfo::class)) //FIXME causes an error
    }

    //TODO Proper pipeline with different stages
    private val registeredSystems: MutableSet<TickingSystem> = mutableSetOf()
    private val registeredListeners: MutableSet<GearyListener> = mutableSetOf()

    override fun getOrRegisterComponentIdForClass(kClass: KClass<*>): GearyComponentId =
        classToComponentMap.getOrElse(kClass) { registerComponentIdForClass(kClass) }

    override fun registerComponentIdForClass(kClass: KClass<*>): GearyComponentId =
        classToComponentMap.getOrPut(kClass) { entity().id }.also {
            it.toGeary().set(ComponentInfo(kClass))
        }

    override fun addSystem(system: GearySystem) {
        // Track systems right at startup since they are likely going to tick very soon anyways and we don't care about
        // any hiccups at that point.
        when (system) {
            is TickingSystem -> {
                if (system in registeredSystems) return
                QueryManager.trackQuery(system)
                registeredSystems.add(system)
            }
            is GearyListener -> {
                if (system in registeredListeners) return
                QueryManager.trackEventListener(system)
                registeredListeners.add(system)
            }
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

    /** Describes how to individually tick each system */
    protected open fun TickingSystem.runSystem() {
        doTick()
    }

    override fun scheduleSystemTicking() {
        TODO("Implement a system for ticking independent of spigot")
    }


    @Synchronized
    override fun getNextId(): GearyEntityId {
        val id = if (!removedEntities.isEmpty()) removedEntities.pop() else currId++
        setRecord(id, rootArchetype.addEntityWithData(id, listOf()))
        return id
    }

    override fun getComponentsFor(entity: GearyEntityId): Set<GearyComponent> =
        getRecord(entity).run {
            archetype.getComponents(row).toSet()
        }

    override fun getRelationsFor(
        entity: GearyEntityId,
        relationValueId: RelationValueId
    ): Set<Pair<GearyComponent, Relation>> = getRecord(entity).run {
        archetype
            .relationsByValue[relationValueId.id.toLong()]
            ?.mapNotNullTo(mutableSetOf()) {
                archetype[row, it.key.withRole(HOLDS_DATA)]?.to(Relation.of(it.key, relationValueId))
            }
    } ?: setOf()


    override fun addComponentFor(
        entity: GearyEntityId,
        componentId: GearyComponentId,
        noEvent: Boolean
    ) {
        getOrAddRecord(entity).apply {
            val newRecord = archetype.addComponent(entity, row, HOLDS_DATA.inv() and componentId)
            typeMap[entity] = newRecord ?: return
            if (!noEvent)
                Engine.temporaryEntity { componentAddEvent ->
                    componentAddEvent.setRelation(componentId, AddedComponent(), noEvent = true)
                    newRecord.archetype.callEvent(componentAddEvent, newRecord.row)
                }
        }
    }

    override fun setComponentFor(
        entity: GearyEntityId,
        componentId: GearyComponentId,
        data: GearyComponent,
        noEvent: Boolean
    ) {
        getOrAddRecord(entity).apply {
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val role = if (!componentId.hasRole(RELATION)) HOLDS_DATA else NO_ROLE
            val componentWithRole = componentId.withRole(role)
            val newRecord = archetype.setComponent(entity, row, componentWithRole, data)
            typeMap[entity] = newRecord ?: return
            if (!noEvent)
                Engine.temporaryEntity { componentAddEvent ->
                    componentAddEvent.setRelation(componentWithRole, AddedComponent(), noEvent = true)
                    newRecord.archetype.callEvent(componentAddEvent, newRecord.row)
                }
        }
    }

    override fun removeComponentFor(entity: GearyEntityId, componentId: GearyComponentId): Boolean {
        return getRecord(entity).apply {
            val record = archetype.removeComponent(entity, row, componentId.withRole(HOLDS_DATA))
                ?: archetype.removeComponent(entity, row, componentId)
            typeMap[entity] = record ?: return false
        } != null
    }

    override fun getComponentFor(entity: GearyEntityId, componentId: GearyComponentId): GearyComponent? =
        getRecord(entity)?.run {
            archetype[row, componentId.let { if (it.hasRole(RELATION)) it else it.withRole(HOLDS_DATA) }]
        }

    override fun hasComponentFor(entity: GearyEntityId, componentId: GearyComponentId): Boolean =
        getRecord(entity)?.archetype?.contains(componentId) ?: false

    //TODO might be a smarter way of storing removed entities as an implicit list within a larger list of entities eventually
    override fun removeEntity(entity: GearyEntityId) {
        // remove all children of this entity from the ECS as well
        entity.toGeary().apply {
            children.forEach { it.removeEntity() }
        }

        clearEntity(entity)

        //add current id into queue for reuse
        removedEntities.push(entity)
    }

    override fun clearEntity(entity: GearyEntityId) {
        getRecord(entity)?.apply {
            archetype.removeEntity(row)
        }
        typeMap.remove(entity)
    }

    public override fun getType(entity: GearyEntityId): GearyType =
        typeMap[entity]?.archetype?.type ?: GearyType()

    override fun getArchetype(id: Int): Archetype = archetypes[id]

    override fun getArchetype(type: GearyType): Archetype {
        var node = rootArchetype
        type.forEach { compId ->
            node = node.componentAddEdges[compId] ?: createArchetype(node, compId)
        }
        return node
    }

    private fun createArchetype(prevNode: Archetype, componentEdge: GearyComponentId): Archetype {
        val arc = synchronized(archetypes) {
            Archetype(prevNode.type.plus(componentEdge), archetypes.size).also {
                archetypes += it
            }
        }
        arc.componentRemoveEdges[componentEdge] = prevNode
        prevNode.componentAddEdges[componentEdge] = arc
        QueryManager.registerArchetype(arc)
        return arc
    }


    public override fun getRecord(entity: GearyEntityId): Record = typeMap[entity]

    override fun setRecord(entity: GearyEntityId, record: Record) {
        typeMap[entity] = record
    }

    private fun getOrAddRecord(entity: GearyEntityId): Record {
        if (!typeMap.contains(entity)) {
            val record = rootArchetype.addEntityWithData(entity, listOf())
            typeMap[entity] = record
            return record
        }
        return typeMap[entity]
    }
}
