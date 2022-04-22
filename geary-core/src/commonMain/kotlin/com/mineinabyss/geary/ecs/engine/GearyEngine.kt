package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.temporaryEntity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.systems.*
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.ecs.entities.parents
import com.mineinabyss.geary.ecs.entities.removeParent
import com.mineinabyss.geary.ecs.events.AddedComponent
import com.mineinabyss.geary.ecs.events.EntityRemoved
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.*
import org.koin.core.component.inject
import org.koin.core.logger.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Synchronized
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * The default implementation of Geary's Engine.
 *
 * This engine uses [Archetype]s. Each component is an entity itself with an id associated with it.
 * We keep track of each entity's components in the form of it's [GearyType] stored in the [typeMap].
 *
 * Learn more [here](https://github.com/MineInAbyss/Geary/wiki/Basic-ECS-engine-architecture).
 */
public open class GearyEngine(override val tickDuration: Duration) : TickingEngine(), QueryContext {
    protected val logger: Logger by inject()

    @PublishedApi
    internal val typeMap: TypeMap = TypeMap()
    override val queryManager: QueryManager by inject()
    private val currId = atomic(0L)
    final override val rootArchetype: Archetype = Archetype(this, GearyType(), 0)
    private val archetypes = mutableListOf(rootArchetype)
    private val removedEntities = EntityStack()
    private val classToComponentMap = ClassToComponentMap()
    override val coroutineContext: CoroutineContext =
        (CoroutineScope(Dispatchers.Default) + CoroutineName("Geary Engine")).coroutineContext

    public val archetypeCount: Int get() = archetypes.size

    internal fun init() {
        //Register an entity for the ComponentInfo component, otherwise getComponentIdForClass does a StackOverflow
        val componentInfo = newEntity()
        classToComponentMap[ComponentInfo::class] = componentInfo.id
        componentInfo.set(ComponentInfo(ComponentInfo::class), noEvent = true)
    }

    override fun start(): Boolean {
        return super.start().also {
            if (it) {
                init()
            }
        }
    }

    private val registeredSystems: MutableSet<TickingSystem> = mutableSetOf()
    private val registeredListeners: MutableSet<GearyListener> = mutableSetOf()

    /** Describes how to individually tick each system */
    protected open suspend fun TickingSystem.runSystem() {
        doTick()
    }

    private fun createArchetype(prevNode: Archetype, componentEdge: GearyComponentId): Archetype {
        val arc = Archetype(this, prevNode.type.plus(componentEdge), archetypes.size).also {
            archetypes += it
        }
        arc.componentRemoveEdges[componentEdge] = prevNode
        prevNode.componentAddEdges[componentEdge] = arc
        queryManager.registerArchetype(arc)
        return arc
    }

    override fun newEntity(initialComponents: Collection<GearyComponent>): GearyEntity {
        val entity = try {
            removedEntities.pop()
        } catch (e: Exception) {
            currId.getAndIncrement().toGeary()
        }
        createRecord(entity, initialComponents)
        return entity
    }

    private fun createRecord(entity: GearyEntity, initialComponents: Collection<GearyComponent>) {
        val ids = initialComponents.map { componentId(it::class) or HOLDS_DATA }
        val addTo = getArchetype(GearyType(ids))
        val record = Record(rootArchetype, -1)
        addTo.addEntityWithData(
            record,
            initialComponents.toTypedArray().apply { sortBy { addTo.indexOf(componentId(it::class)) } },
            entity,
        )
        typeMap.set(entity, record)
    }

    override fun addSystem(system: GearySystem) {
        // Track systems right at startup since they are likely going to tick very soon anyways and we don't care about
        // any hiccups at that point.
        when (system) {
            is TickingSystem -> {
                if (system in registeredSystems) return
                queryManager.trackQuery(system)
                registeredSystems.add(system)
            }
            is GearyListener -> {
                if (system in registeredListeners) return
                system.start()
                queryManager.trackEventListener(system)
                registeredListeners.add(system)
            }
            else -> system.onStart()
        }
    }

    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
    override fun unsafeRecord(entity: GearyEntity): Record =
        typeMap.get(entity)

    override fun setRecord(entity: GearyEntity, record: Record) {
        typeMap.set(entity, record)
    }

    override fun getComponentFor(entity: GearyEntity, componentId: GearyComponentId): GearyComponent? {
        val (archetype, row) = unsafeRecord(entity)
        entity.unsafeRecord()
        return archetype[row, componentId.let { if (it.hasRole(RELATION)) it else it.withRole(HOLDS_DATA) }]
    }

    override fun scheduleSystemTicking() {
        TODO("Implement a system for ticking independent of spigot")
    }

    override fun getComponentsFor(entity: GearyEntity): Array<GearyComponent> {
        val (archetype, row) = unsafeRecord(entity)
        return archetype.getComponents(row).also { array ->
            for (relation in archetype.relations) {
                val i = archetype.indexOf(relation.id)
                array[i] = RelationComponent(relation.key, array[i])
            }
        }
    }

    override fun getRelationsFor(
        entity: GearyEntity,
        relationValueId: RelationValueId
    ): Set<Pair<GearyComponent, Relation>> {
        val (archetype, row) = unsafeRecord(entity)
        return archetype
            .relationsByValue[relationValueId.id.toLong()]
            ?.mapNotNullTo(mutableSetOf()) {
                archetype[row, it.key.withRole(HOLDS_DATA)]?.to(Relation.of(it.key, relationValueId))
            } ?: setOf()
    }


    override fun addComponentFor(
        entity: GearyEntity,
        componentId: GearyComponentId,
        noEvent: Boolean
    ) {
        unsafeRecord(entity).apply {
            archetype.addComponent(this, HOLDS_DATA.inv() and componentId) || return

            if (!noEvent) temporaryEntity { componentAddEvent ->
                componentAddEvent.setRelation(componentId, AddedComponent(), noEvent = true)
                archetype.callEvent(componentAddEvent, row)
            }
        }
    }

    override fun setComponentFor(
        entity: GearyEntity,
        componentId: GearyComponentId,
        data: GearyComponent,
        noEvent: Boolean
    ) {
        unsafeRecord(entity).apply {
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val role = if (!componentId.hasRole(RELATION)) HOLDS_DATA else NO_ROLE
            val componentWithRole = componentId.withRole(role)
            archetype.setComponent(this, componentWithRole, data) || return

            if (!noEvent) temporaryEntity { componentAddEvent ->
                componentAddEvent.setRelation(componentWithRole, AddedComponent(), noEvent = true)
                archetype.callEvent(componentAddEvent, row)
            }
        }
    }

    override fun removeComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean {
        unsafeRecord(entity).apply {
            archetype.removeComponent(this, componentId.withRole(HOLDS_DATA))
                    || archetype.removeComponent(this, componentId)
        }
        return true
    }

    override fun hasComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean =
        unsafeRecord(entity).archetype.contains(componentId)

    override fun removeEntity(entity: GearyEntity, event: Boolean) {
        if (event) entity.callEvent(EntityRemoved())

        // remove all children of this entity from the ECS as well
        entity.apply {
            children.forEach {
                // Remove self from the child's parents or remove the child if it no longer has parents
                if (parents == setOf(this)) it.removeEntity(event)
                else it.removeParent(this)
            }
        }

        val (archetype, row) = unsafeRecord(entity)
        archetype.scheduleRemoveRow(row)
        typeMap.remove(entity)
        //add current id into queue for reuse
        removedEntities.push(entity)
    }

    override fun clearEntity(entity: GearyEntity) {
        // TODO might make sense as non blocking?
        val (archetype, row) = unsafeRecord(entity)
        archetype.scheduleRemoveRow(row)
        rootArchetype.addEntityWithData(unsafeRecord(entity), arrayOf())
    }

    override fun getArchetype(id: Int): Archetype = archetypes[id]

    @Synchronized //TODO multiplatform support
    override fun getArchetype(type: GearyType): Archetype {
        var node = rootArchetype
        type.forEach { compId ->
            node =
                if (compId in node.componentAddEdges) node.componentAddEdges[compId]
                else createArchetype(node, compId)
        }
        return node
    }

    private val classToComponentMapLock = SynchronizedObject()
    override fun getOrRegisterComponentIdForClass(kClass: KClass<*>): GearyComponentId {
        synchronized(classToComponentMapLock) {
            val id = classToComponentMap[kClass]
            if (id == (-1L).toULong()) return registerComponentIdForClass(kClass)
            return id
        }
    }

    private fun registerComponentIdForClass(kClass: KClass<*>): GearyComponentId {
        val compEntity = newEntity(initialComponents = listOf(ComponentInfo(kClass)))
        classToComponentMap[kClass] = compEntity.id
        return compEntity.id
    }

    internal fun scheduleRemove(archetype: Archetype) {
        queuedCleanup += archetype
    }

    //TODO what data structure is most efficient here?
    private val queuedCleanup = mutableSetOf<Archetype>()
    private val runningAsyncJobs = mutableSetOf<Job>()
    private var iterationJob: Job? = null
    private val safeDispatcher = Dispatchers.Default.limitedParallelism(1)

    override fun runSafely(scope: CoroutineScope, job: Job) {
        launch(safeDispatcher) {
            iterationJob?.join()
            runningAsyncJobs += job
            job.invokeOnCompletion {
//                launch(safeDispatcher) {
                runningAsyncJobs -= job
                if (it != null) throw it
//                }
            }
            job.start()
        }
    }

    override suspend fun tick(currentTick: Long): Unit = coroutineScope {
        // Create a job but don't start it
        val job = launch(start = CoroutineStart.LAZY) {
            cleanup()
            registeredSystems
                .filter { currentTick % (it.interval / tickDuration).toInt().coerceAtLeast(1) == 0L }
                .forEach {
                    try {
                        it.runSystem()
                    } catch (e: Exception) {
                        logger.error("Error while running system ${it::class.simpleName}")
                        e.printStackTrace()
                    }
                }
        }
        // Await completion of all other jobs
        iterationJob = job
        withContext(safeDispatcher) {
            runningAsyncJobs.joinAll()
        }

        // Tick all systems
        logger.debug("Started engine tick")
        job.start()
        job.join()
        iterationJob = null
        logger.debug("Finished engine tick")
    }

    internal fun cleanup() {
        queuedCleanup.forEach { archetype ->
            archetype.cleanup()
        }
        queuedCleanup.clear()
    }
}
