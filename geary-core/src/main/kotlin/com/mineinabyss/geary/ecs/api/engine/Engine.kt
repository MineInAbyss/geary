package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
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

    public val rootArchetype: Archetype

    /** Get the next free ID for use with the ECS. */
    public fun getNextId(): GearyEntityId

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: GearySystem)

    /** Gets a list of all the components [entity] has. */
    public fun getComponentsFor(entity: GearyEntityId): Set<GearyComponent>

    /**
     * Gets a list of components related to the component represented by [relationDataType], and pairs them with
     * the id of the child part of the relation.
     */
    public fun getRelationsFor(
        entity: GearyEntityId,
        relationDataType: RelationValueId
    ): Set<Pair<GearyComponent, Relation>>

    /** Gets a [componentId]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public fun getComponentFor(entity: GearyEntityId, componentId: GearyComponentId): GearyComponent?

    /** Checks whether an [entity] has a [componentId] */
    public fun hasComponentFor(entity: GearyEntityId, componentId: GearyComponentId): Boolean

    /** Adds this [component] to the [entity]'s type but doesn't store any data. */
    public fun addComponentFor(entity: GearyEntityId, componentId: GearyComponentId, noEvent: Boolean)

    /** Associates this component's data with this entity. */
    public fun setComponentFor(
        entity: GearyEntityId,
        componentId: GearyComponentId,
        data: GearyComponent,
        noEvent: Boolean
    )

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    public fun removeComponentFor(entity: GearyEntityId, componentId: GearyComponentId): Boolean

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: GearyEntityId)

    /** Clears all components on an entity. */
    public fun clearEntity(entity: GearyEntityId)

    /**
     * Given a component's [kClass], returns its [GearyComponentId], or registers the component with the ECS
     * @see registerComponentIdForClass
     */
    public fun getOrRegisterComponentIdForClass(kClass: KClass<*>): GearyComponentId

    /** Registers an entity with a [ComponentInfo] component that will represent this [kClass]'s component type. */
    public fun registerComponentIdForClass(kClass: KClass<*>): GearyComponentId

    /** Gets the [GearyType] of this [entity] (i.e. a list of all the component/entity ids it holds) */
    public fun getType(entity: GearyEntityId): GearyType

    public fun getArchetype(id: Int): Archetype

    public fun getArchetype(type: GearyType): Archetype

    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
    public fun getRecord(entity: GearyEntityId): Record

    /** Updates the record of a given entity*/
    public fun setRecord(entity: GearyEntityId, record: Record)

}
