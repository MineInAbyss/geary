package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.*
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.services.gearyService
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
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
    public fun addSystem(system: TickingSystem): Boolean

    /** Gets a list of all the components [entity] has. */
    public fun getComponentsFor(entity: GearyEntityId): Set<GearyComponent>

    /** Gets a [component]'s data from an [entity] or null if not present/the component doesn't hold any data. */
    public fun getComponentFor(entity: GearyEntityId, component: GearyComponentId): GearyComponent?

    /** Checks whether entity of [entity] has an active [component type][kClass] */
    public fun hasComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /** Adds this [component] to the [entity]'s type but doesnt store any data. */
    public fun addComponentFor(entity: GearyEntityId, component: GearyComponentId)

    /** Associates this component's data with this entity. */
    public fun setComponentFor(entity: GearyEntityId, component: GearyComponentId, data: GearyComponent)

    public fun setRelationFor(entity: GearyEntityId, trait: GearyComponentId, forComponent: GearyComponentId, data: GearyComponent)

    /** Removes a [component] from an [entity] and clears any data previously associated with it. */
    public fun removeComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: GearyEntityId)

    //TODO split registry and getting
    /** Given a component's [kClass], returns its [GearyComponentId], or registers the component with the ECS */
    public fun getComponentIdForClass(kClass: KClass<*>): GearyComponentId

    public fun getType(entity: GearyEntityId): GearyType

    public fun setRecord(entity: GearyEntityId, record: Record)
}
