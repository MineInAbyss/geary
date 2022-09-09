package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.components.CouldHaveChildren
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.context.QueryContext
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ClassToComponentMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.engine.EventRunner
import com.mineinabyss.geary.engine.TickingEngine
import com.mineinabyss.geary.helpers.parents
import com.mineinabyss.geary.helpers.removeParent
import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.QueryManager
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.RelationWithData
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.*
import org.koin.core.component.inject
import org.koin.core.logger.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * The default implementation of Geary's Engine.
 *
 * This engine uses [Archetype]s. Each component is an entity itself with an id associated with it.
 * We keep track of each entity's components in the form of it's [EntityType] stored in the [typeMap].
 *
 * Learn more [here](https://github.com/MineInAbyss/Geary/wiki/Basic-ECS-engine-architecture).
 */
public open class ArchetypeEngine(override val tickDuration: Duration) : TickingEngine(), QueryContext {
    protected val logger: Logger by inject()
    override val queryManager: QueryManager by inject()

    internal val archetypeProvider: ArchetypeProvider by inject()
    internal val entityProvider: EntityProvider by inject()
    internal val eventRunner: EventRunner by inject()
    @PublishedApi
    internal val typeMap: TypeMap by inject()

    private val classToComponentMap = ClassToComponentMap()
    override val coroutineContext: CoroutineContext =
        (CoroutineScope(Dispatchers.Default) + CoroutineName("Geary Engine")).coroutineContext

    private val classToComponentMapLock = SynchronizedObject()

    private val registeredSystems: MutableSet<RepeatingSystem> = mutableSetOf()
    private val registeredListeners: MutableSet<Listener> = mutableSetOf()

    //TODO what data structure is most efficient here?
    private val queuedCleanup = mutableSetOf<Archetype>()
    private val runningAsyncJobs = mutableSetOf<Job>()
    private var iterationJob: Job? = null
    private val safeDispatcher = Dispatchers.Default.limitedParallelism(1)

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

    /** Describes how to individually tick each system */
    protected open suspend fun RepeatingSystem.runSystem() {
        doTick()
    }

    override fun newEntity(initialComponents: Collection<Component>): Entity {
        return entityProvider.newEntity(initialComponents)
    }

    override fun addSystem(system: GearySystem) {
        // Track systems right at startup since they are likely going to tick very soon anyways and we don't care about
        // any hiccups at that point.
        when (system) {
            is RepeatingSystem -> {
                if (system in registeredSystems) return
                queryManager.trackQuery(system)
                registeredSystems.add(system)
            }

            is Listener -> {
                if (system in registeredListeners) return
                system.start()
                queryManager.trackEventListener(system)
                registeredListeners.add(system)
            }

            else -> system.onStart()
        }
    }

    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
    internal fun getRecord(entity: Entity): Record =
        typeMap.get(entity)

    /** Updates the record of a given entity */
    internal fun setRecord(entity: Entity, record: Record) {
        typeMap.set(entity, record)
    }

    override fun getComponentFor(entity: Entity, componentId: ComponentId): Component? {
        val (archetype, row) = getRecord(entity)
        return archetype[row, componentId.let { if (it.hasRole(RELATION)) it else it.withRole(HOLDS_DATA) }]
    }

    override fun scheduleSystemTicking() {
        var tick = 0L
        launch {
            while (true) {
                tick(tick++)
                delay(tickDuration)
            }
        }
    }

    override fun getComponentsFor(entity: Entity): Array<Component> {
        val (archetype, row) = getRecord(entity)
        return archetype.getComponents(row).also { array ->
            for (relation in archetype.relationsWithData) {
                val i = archetype.indexOf(relation.id)
                array[i] = RelationWithData(array[i], null, relation)
            }
        }
    }

    override fun getRelationsWithDataFor(
        entity: Entity,
        kind: ComponentId,
        target: EntityId,
    ): List<RelationWithData<*, *>> {
        val (arc, row) = getRecord(entity)
        return arc.getRelations(kind, target).map { relation ->
            RelationWithData(
                data = if (kind.hasRole(HOLDS_DATA)) arc[row, relation.id] else null,
                targetData = if (target.hasRole(HOLDS_DATA)) arc[row, relation.target.withRole(HOLDS_DATA)] else null,
                relation = relation
            )
        }
    }

    override fun getRelationsFor(entity: Entity, kind: ComponentId, target: EntityId): List<Relation> =
        getRecord(entity).archetype.getRelations(kind, target)

    override fun addComponentFor(
        entity: Entity,
        componentId: ComponentId,
        noEvent: Boolean
    ) {
        getRecord(entity).apply {
            archetype.addComponent(this, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
    }

    override fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    ) {
        getRecord(entity).apply {
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val componentWithRole = componentId.withRole(HOLDS_DATA)
            archetype.setComponent(this, componentWithRole, data, !noEvent)
        }
    }

    override fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        getRecord(entity).run {
            val a = archetype.removeComponent(this, componentId.withRole(HOLDS_DATA))
            val b = archetype.removeComponent(this, componentId.withoutRole(HOLDS_DATA))
            a || b // return whether anything was changed
        }

    override fun hasComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        componentId in getRecord(entity).archetype

    override fun removeEntity(entity: Entity, event: Boolean) {
        if (event) entity.callEvent {
            add<EntityRemoved>()
        }

        // remove all children of this entity from the ECS as well
        if (entity.has<CouldHaveChildren>()) entity.apply {
            children.forEach {
                // Remove self from the child's parents or remove the child if it no longer has parents
                if (it.parents == setOf(this)) it.removeEntity(event)
                else it.removeParent(this)
            }
        }

        val (archetype, row) = getRecord(entity)
        archetype.scheduleRemoveRow(row)
        //add current id into queue for reuse
        entityProvider.removeEntity(entity)
    }

    override fun clearEntity(entity: Entity) {
        val (archetype, row) = getRecord(entity)
        archetype.scheduleRemoveRow(row)
        archetypeProvider.rootArchetype.addEntityWithData(getRecord(entity), arrayOf(), entity)
    }

    override fun getType(entity: Entity): EntityType = getRecord(entity).archetype.type

    override fun getOrRegisterComponentIdForClass(kClass: KClass<*>): ComponentId =
        synchronized(classToComponentMapLock) {
            val id = classToComponentMap[kClass]
            if (id == (-1L).toULong()) return registerComponentIdForClass(kClass)
            return id
        }

    private fun registerComponentIdForClass(kClass: KClass<*>): ComponentId {
        val compEntity = newEntity(initialComponents = listOf(ComponentInfo(kClass)))
        classToComponentMap[kClass] = compEntity.id
        return compEntity.id
    }

    internal fun scheduleRemove(archetype: Archetype) {
        queuedCleanup += archetype
    }

    override fun runSafely(scope: CoroutineScope, job: Job) {
        launch(safeDispatcher) {
            iterationJob?.join()
            runningAsyncJobs += job
            job.invokeOnCompletion {
                launch(safeDispatcher) {
                    runningAsyncJobs -= job
                }
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

    override fun callEvent(target: Entity, event: Entity, source: Entity?) {
        eventRunner.callEvent(getRecord(target), getRecord(event), source?.let { getRecord(it) })
    }

    private fun cleanup() {
        queuedCleanup.forEach { archetype ->
            archetype.cleanup()
        }
        queuedCleanup.clear()
    }
}
