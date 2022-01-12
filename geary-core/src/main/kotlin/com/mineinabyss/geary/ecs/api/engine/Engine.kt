package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.services.gearyService
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.Record
import kotlin.reflect.KClass

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
public interface Engine {
    public companion object : Engine by gearyService()

    /** The root archetype representing a type of no components */
    public val rootArchetype: Archetype

    /** Get the smallest free entity ID. */
    public fun newEntity(): GearyEntity

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: GearySystem)

    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public fun getComponentFor(entity: GearyEntity, componentId: GearyComponentId): GearyComponent?

    /** Gets a list of all the components [entity] has. */
    public fun getComponentsFor(entity: GearyEntity): Set<GearyComponent>

    //TODO clean up so it's consistent with Accessor format
    /**
     * Gets a list of components related to the component represented by [relationDataType], and pairs them with
     * the id of the child part of the relation.
     */
    public fun getRelationsFor(
        entity: GearyEntity,
        relationValueId: RelationValueId
    ): Set<Pair<GearyComponent, Relation>>


    /** Checks whether an [entity] has a [componentId] */
    public fun hasComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean

    /** Adds this [component] to the [entity]'s type but doesn't store any data. */
    public fun addComponentFor(entity: GearyEntity, componentId: GearyComponentId, noEvent: Boolean)

    /** Associates this component's data with this entity. */
    public fun setComponentFor(
        entity: GearyEntity,
        componentId: GearyComponentId,
        data: GearyComponent,
        noEvent: Boolean
    )

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    public fun removeComponentFor(entity: GearyEntity, componentId: GearyComponentId): Boolean

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: GearyEntity)

    /** Clears all components on an entity. */
    public fun clearEntity(entity: GearyEntity)

    /**
     * Given a component's [kClass], returns its [GearyComponentId], or registers an entity
     * with a [ComponentInfo] that will represent this [kClass]'s component type.
     */
    public fun getOrRegisterComponentIdForClass(kClass: KClass<*>): GearyComponentId

    public fun getArchetype(id: Int): Archetype

    public fun getArchetype(type: GearyType): Archetype

    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
    public fun getRecord(entity: GearyEntity): Record

    /** Updates the record of a given entity*/
    public fun setRecord(entity: GearyEntity, record: Record)

}
