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
public interface Engine : KoinComponent, EngineContext, CoroutineScope {
    override val engine: Engine get() = this

    /** Get the smallest free entity ID. */
    public fun newEntity(initialComponents: Collection<Component> = listOf()): Entity

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: GearySystem)

    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public fun getComponentFor(entity: Entity, componentId: ComponentId): Component?

    /** Gets a list of all the components [entity] has, as well as relations in the form of [RelationComponent]. */
    public fun getComponentsFor(entity: Entity): Array<Component>

    /**
     * Gets relations in the same format as [Archetype.getRelations], but when kind/target [HOLDS_DATA], the appropriate
     * data is written to a [RelationWithData] object.
     */
    public fun getRelationsWithDataFor(
        entity: Entity,
        kind: ComponentId,
        target: EntityId
    ): List<RelationWithData<*, *>>

    public fun getRelationsFor(entity: Entity, kind: ComponentId, target: EntityId): List<Relation>

    /** Checks whether an [entity] has a [componentId] */
    public fun hasComponentFor(entity: Entity, componentId: ComponentId): Boolean

    /** Adds this [componentId] to the [entity]'s type but doesn't store any data. */
    public fun addComponentFor(entity: Entity, componentId: ComponentId, noEvent: Boolean)

    /**
     * Sets [data] under a [componentId] for an [entity].
     *
     * @param noEvent Whether to fire an [AddedComponent] event.
     */
    public fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    )

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    public fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: Entity, event: Boolean = true)

    /** Removes all components from an entity. */
    public fun clearEntity(entity: Entity)

    /** Gets an [entity]'s type */
    public fun getType(entity: Entity): EntityType

    /**
     * Given a component's [kClass], returns its [ComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    public fun getOrRegisterComponentIdForClass(kClass: KClass<*>): ComponentId

    public fun runSafely(scope: CoroutineScope, job: Job)

    /** Calls an event on [target] with data in an [event] entity, optionally with a [source] entity. */
    public fun callEvent(target: Entity, event: Entity, source: Entity?)
}
