package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.api.services.gearyService
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.engine.Record
import kotlin.reflect.KClass

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
public interface Engine {
    public companion object : Engine by gearyService()

    /** Get the next free ID for use with the ECS. */
    public fun getNextId(): GearyEntityId

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: GearySystem)

    /** Gets a list of all the components [entity] has. */
    public fun getComponentsFor(entity: GearyEntityId): Set<GearyComponent>

    /**
     * Gets a list of components related to the component represented by [relationParent], and pairs them with
     * the id of the child part of the relation.
     */
    public fun getRelationsFor(
        entity: GearyEntityId,
        relationParent: RelationParent
    ): Set<Pair<GearyComponent, Relation>>

    /** Gets a [component]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public fun getComponentFor(entity: GearyEntityId, component: GearyComponentId): GearyComponent?

    /** Checks whether an [entity] has a [component] */
    public fun hasComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /** Adds this [component] to the [entity]'s type but doesn't store any data. */
    public fun addComponentFor(entity: GearyEntityId, component: GearyComponentId)

    /** Associates this component's data with this entity. */
    public fun setComponentFor(entity: GearyEntityId, component: GearyComponentId, data: GearyComponent)

    /** Sets a [Relation] component for this [entity], with [data] associated with the [parent] */
    public fun setRelationFor(
        entity: GearyEntityId,
        parent: RelationParent,
        forComponent: GearyComponentId,
        data: GearyComponent
    )

    /** Removes a [component] from an [entity] and clears any data previously associated with it. */
    public fun removeComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

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

    //TODO move this somewhere more internal
    /** Updates the record of a given entity*/
    public fun getRecord(entity: GearyEntityId): Record?

    /** Updates the record of a given entity*/
    public fun setRecord(entity: GearyEntityId, record: Record)

}
