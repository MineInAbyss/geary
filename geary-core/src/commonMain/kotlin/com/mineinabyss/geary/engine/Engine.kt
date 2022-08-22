package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.components.events.AddedComponent
import com.mineinabyss.geary.context.EngineContext
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.geary.systems.accessors.RelationWithData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
public abstract class Engine : KoinComponent, EngineContext, CoroutineScope {
    override val engine: Engine get() = this

    /** Get the smallest free entity ID. */
    public abstract fun newEntity(initialComponents: Collection<Component> = listOf()): Entity

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public abstract fun addSystem(system: GearySystem)

    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public abstract fun getComponentFor(entity: Entity, componentId: ComponentId): Component?

    /** Gets a list of all the components [entity] has, as well as relations in the form of [RelationComponent]. */
    public abstract fun getComponentsFor(entity: Entity): Array<Component>

    /**
     * Gets relations in the same format as [Archetype.getRelations], but when kind/target [HOLDS_DATA], the appropriate
     * data is written to a [RelationWithData] object.
     */
    public abstract fun getRelationsWithDataFor(
        entity: Entity,
        kind: ComponentId,
        target: EntityId
    ): List<RelationWithData<*, *>>

    /** Checks whether an [entity] has a [componentId] */
    public abstract fun hasComponentFor(entity: Entity, componentId: ComponentId): Boolean

    /** Adds this [componentId] to the [entity]'s type but doesn't store any data. */
    public abstract fun addComponentFor(entity: Entity, componentId: ComponentId, noEvent: Boolean)

    /**
     * Sets [data] under a [componentId] for an [entity].
     *
     * @param noEvent Whether to fire an [AddedComponent] event.
     */
    public abstract fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    )

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    public abstract fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean

    /** Removes an entity from the ECS, freeing up its entity id. */
    public abstract fun removeEntity(entity: Entity, event: Boolean = true)

    /** Removes all components from an entity. */
    public abstract fun clearEntity(entity: Entity)

    /**
     * Given a component's [kClass], returns its [ComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    public abstract fun getOrRegisterComponentIdForClass(kClass: KClass<*>): ComponentId

    /** Gets an archetype by id or throws an error if it doesn't exist in this engine. */
    public abstract fun getArchetype(id: Int): Archetype

    /** Gets or creates an archetype from a [type]. */
    public abstract fun getArchetype(type: EntityType): Archetype

    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
    internal abstract fun getRecord(entity: Entity): Record

    /** Updates the record of a given entity */
    public abstract fun setRecord(entity: Entity, record: Record)

    public abstract fun runSafely(scope: CoroutineScope, job: Job)
}
