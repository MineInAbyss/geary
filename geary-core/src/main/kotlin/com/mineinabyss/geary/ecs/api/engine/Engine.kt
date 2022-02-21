package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.Record
import com.mineinabyss.geary.ecs.events.AddedComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
public abstract class Engine : KoinComponent, EngineScope, CoroutineScope {
    override val engine: Engine get() = this

    /** The root archetype representing a type of no components */
    public abstract val rootArchetype: Archetype


    /** Get the smallest free entity ID. */
    public abstract fun newEntity(initialComponents: Collection<GearyComponent> = listOf()): GearyEntity

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public abstract fun addSystem(system: GearySystem)

    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public abstract fun getComponentFor(entity: GearyEntity, componentId: GearyComponentId): GearyComponent?

    /** Gets a list of all the components [entity] has, as well as relations in the form of [RelationComponent]. */
    public abstract fun getComponentsFor(entity: GearyEntity): Array<GearyComponent>

    //TODO clean up so it's consistent with Accessor's relation format
    /**
     * Gets a list of relations on [entity] with to value [relationValueId].
     */
    public abstract fun getRelationsFor(
        entity: GearyEntity,
        relationValueId: RelationValueId
    ): Set<Pair<GearyComponent, Relation>>


    /** Checks whether an [entity] has a [componentId] */
    public abstract fun hasComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean

    /** Adds this [componentId] to the [entity]'s type but doesn't store any data. */
    public abstract fun addComponentFor(entity: GearyEntity, componentId: GearyComponentId, noEvent: Boolean)

    /**
     * Sets [data] under a [componentId] for an [entity].
     *
     * @param noEvent Whether to fire an [AddedComponent] event.
     */
    public abstract fun setComponentFor(
        entity: GearyEntity,
        componentId: GearyComponentId,
        data: GearyComponent,
        noEvent: Boolean
    )

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    public abstract fun removeComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean

    /** Removes an entity from the ECS, freeing up its entity id. */
    public abstract fun removeEntity(entity: GearyEntity, event: Boolean = true)

    /** Removes all components from an entity. */
    public abstract fun clearEntity(entity: GearyEntity)

    /**
     * Given a component's [kClass], returns its [GearyComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    public abstract fun getOrRegisterComponentIdForClass(kClass: KClass<*>): GearyComponentId

    /** Gets an archetype by id or throws an error if it doesn't exist in this engine. */
    public abstract fun getArchetype(id: Int): Archetype

    /** Gets or creates an archetype from a [type]. */
    public abstract fun getArchetype(type: GearyType): Archetype

    internal abstract fun unsafeRecord(entity: GearyEntity): Record

//    @PublishedApi
//    internal abstract suspend fun lock(entity: GearyEntity)

//    @PublishedApi
//    internal abstract fun unlock(entity: GearyEntity)

    /** Updates the record of a given entity */
    public abstract fun setRecord(entity: GearyEntity, record: Record)

    public abstract fun runSafely(scope: CoroutineScope, job: Job)
}
