package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.temporaryEntity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.ecs.entities.children
import com.mineinabyss.geary.ecs.entities.parents
import com.mineinabyss.geary.ecs.entities.removeParent
import com.mineinabyss.geary.ecs.events.AddedComponent
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.time.inWholeTicks
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
    private val classToComponentMap = ClassToComponentMap()

    init {
        //Register an entity for the ComponentInfo component, otherwise getComponentIdForClass does a StackOverflow
        val componentInfo = entity()
        classToComponentMap[ComponentInfo::class] = componentInfo.id
//        componentInfo.set(ComponentInfo(ComponentInfo::class)) //FIXME causes an error
    }

    private val registeredSystems: MutableSet<TickingSystem> = mutableSetOf()
    private val registeredListeners: MutableSet<GearyListener> = mutableSetOf()

    /** Describes how to individually tick each system */
    protected open fun TickingSystem.runSystem() {
        doTick()
    }

    private fun getOrCreateRecord(entity: GearyEntity): Record {
        if (!typeMap.contains(entity)) {
            val record = rootArchetype.addEntityWithData(entity, listOf())
            typeMap[entity] = record
            return record
        }
        return typeMap[entity]
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

    @Synchronized
    override fun newEntity(): GearyEntity {
        val entity = if (!removedEntities.isEmpty()) removedEntities.pop() else (currId++).toGeary()
        setRecord(entity, rootArchetype.addEntityWithData(entity, listOf()))
        return entity
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

    override fun getRecord(entity: GearyEntity): Record = typeMap[entity]

    override fun setRecord(entity: GearyEntity, record: Record) {
        typeMap[entity] = record
    }

    override fun getComponentFor(entity: GearyEntity, componentId: GearyComponentId): GearyComponent? =
        getRecord(entity).run {
            archetype[row, componentId.let { if (it.hasRole(RELATION)) it else it.withRole(HOLDS_DATA) }]
        }

    override fun scheduleSystemTicking() {
        TODO("Implement a system for ticking independent of spigot")
    }

    override fun getComponentsFor(entity: GearyEntity): Set<GearyComponent> =
        getRecord(entity).run {
            archetype.getComponents(row).also { array ->
                for (relation in archetype.relations) {
                    val i = archetype.indexOf(relation.id)
                    array[i] = RelationComponent(relation.key, array[i])
                }
            }.toSet()
        }

    override fun getRelationsFor(
        entity: GearyEntity,
        relationValueId: RelationValueId
    ): Set<Pair<GearyComponent, Relation>> = getRecord(entity).run {
        archetype
            .relationsByValue[relationValueId.id.toLong()]
            ?.mapNotNullTo(mutableSetOf()) {
                archetype[row, it.key.withRole(HOLDS_DATA)]?.to(Relation.of(it.key, relationValueId))
            }
    } ?: setOf()


    override fun addComponentFor(
        entity: GearyEntity,
        componentId: GearyComponentId,
        noEvent: Boolean
    ) {
        getOrCreateRecord(entity).apply {
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
        entity: GearyEntity,
        componentId: GearyComponentId,
        data: GearyComponent,
        noEvent: Boolean
    ) {
        getOrCreateRecord(entity).apply {
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

    override fun removeComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean {
        getRecord(entity).apply {
            val record = archetype.removeComponent(entity, row, componentId.withRole(HOLDS_DATA))
                ?: archetype.removeComponent(entity, row, componentId)
            typeMap[entity] = record ?: return false
        }
        return true
    }

    override fun hasComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean =
        getRecord(entity).archetype.contains(componentId)

    override fun removeEntity(entity: GearyEntity) {
        // remove all children of this entity from the ECS as well
        entity.apply {
            children.forEach {
                // Remove self from the child's parents or remove the child if it no longer has parents
                if (parents == setOf(this)) it.removeEntity()
                else it.removeParent(this)
            }
        }

        clearEntity(entity)

        //add current id into queue for reuse
        removedEntities.push(entity)
    }

    override fun clearEntity(entity: GearyEntity) {
        getRecord(entity).apply {
            archetype.removeEntity(row)
        }
        typeMap.remove(entity)
    }

    override fun getArchetype(id: Int): Archetype = archetypes[id]

    override fun getArchetype(type: GearyType): Archetype {
        var node = rootArchetype
        type.forEach { compId ->
            node =
                if (compId in node.componentAddEdges) node.componentAddEdges[compId]
                else createArchetype(node, compId)
        }
        return node
    }

    override fun getOrRegisterComponentIdForClass(kClass: KClass<*>): GearyComponentId {
        val id = classToComponentMap[kClass]
        if (id == (-1L).toULong()) return registerComponentIdForClass(kClass)
        return id
    }

    private fun registerComponentIdForClass(kClass: KClass<*>): GearyComponentId {
        val compEntity = newEntity()
        classToComponentMap[kClass] = compEntity.id
        compEntity.set(ComponentInfo(kClass))
        return compEntity.id
    }

    override fun tick(currentTick: Long) {
        registeredSystems
            .filter { currentTick % it.interval.inWholeTicks.coerceAtLeast(1) == 0L }
            .forEach {
                try {
                    it.runSystem()
                } catch (e: Exception) {
                    logError("Error while running system ${it.javaClass.name}")
                    e.printStackTrace()
                }
            }
    }
}
