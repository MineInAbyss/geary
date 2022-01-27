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
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
public interface Engine : KoinComponent, EngineScope {
    override val engine: Engine get() = this

    /** The root archetype representing a type of no components */
    public val rootArchetype: Archetype

    /** Get the smallest free entity ID. */
    public fun newEntity(): GearyEntity

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: GearySystem)

    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public fun getComponentFor(entity: GearyEntity, componentId: GearyComponentId): GearyComponent?

    /** Gets a list of all the components [entity] has, as well as relations in the form of [RelationComponent]. */
    public fun getComponentsFor(entity: GearyEntity): Set<GearyComponent>

    //TODO clean up so it's consistent with Accessor's relation format
    /**
     * Gets a list of relations on [entity] with to value [relationValueId].
     */
    public fun getRelationsFor(
        entity: GearyEntity,
        relationValueId: RelationValueId
    ): Set<Pair<GearyComponent, Relation>>


    /** Checks whether an [entity] has a [componentId] */
    public fun hasComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean

    /** Adds this [componentId] to the [entity]'s type but doesn't store any data. */
    public fun addComponentFor(entity: GearyEntity, componentId: GearyComponentId, noEvent: Boolean)

    /**
     * Sets [data] under a [componentId] for an [entity].
     *
     * @param noEvent Whether to fire an [AddedComponent] event.
     */
    public fun setComponentFor(
        entity: GearyEntity,
        componentId: GearyComponentId,
        data: GearyComponent,
        noEvent: Boolean
    )

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    public fun removeComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: GearyEntity, callRemoveEvent: Boolean = true)

    /** Removes all components from an entity. */
    public fun clearEntity(entity: GearyEntity)

    /**
     * Given a component's [kClass], returns its [GearyComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    public fun getOrRegisterComponentIdForClass(kClass: KClass<*>): GearyComponentId

    /** Gets an archetype by id or throws an error if it doesn't exist in this engine. */
    public fun getArchetype(id: Int): Archetype

    /** Gets or creates an archetype from a [type]. */
    public fun getArchetype(type: GearyType): Archetype

    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
    public fun getRecord(entity: GearyEntity): Record

    /** Updates the record of a given entity */
    public fun setRecord(entity: GearyEntity, record: Record)
}
