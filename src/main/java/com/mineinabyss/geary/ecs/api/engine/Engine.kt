package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.*
import com.mineinabyss.geary.ecs.api.systems.Family
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.idofront.plugin.getService
import kotlin.reflect.KClass

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
public interface Engine {
    //TODO stop relying on spigot here for service
    public companion object : Engine by getService()

    /** Get the next free ID for use with the ECS. */
    public fun getNextId(): GearyEntityId

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: TickingSystem): Boolean

    /** Gets a list of all the components [entity] has. */
    public fun getComponentsFor(entity: GearyEntityId): Set<GearyComponent>

    /** Gets a component of type [T] from an [entity]. */
    public fun <T : GearyComponent> getComponentFor(entity: GearyEntityId, component: GearyComponentId): T?

    /** Checks whether an [entity] holds a [component], without regards for whether or not it's active. */
    public fun holdsComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /** Checks whether entity of [entity] has an active [component type][kClass] */
    public fun hasComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /** Adds a [component] of under a [component type][kClass] for entity of [entity].  */
    public fun <T : GearyComponent> addComponentFor(entity: GearyEntityId, component: T): T

    /**
     * Removes a [component] from an [entity]. Will unset it for the entity's type, as well as remove it internally
     * if an actual instance exists.
     *
     * @return Whether the component was present before removal.
     */
    public fun removeComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean


    /** Adds this [component] to the [entity]'s type. */
    public fun setFor(entity: GearyEntityId, component: GearyComponentId)

    /** Removes this [component] from the [entity]'s type. */
    public fun unsetFor(entity: GearyEntityId, component: GearyComponentId)

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: GearyEntityId)

    //TODO split registry and getting
    /** Given a component's [kClass], returns its [GearyComponentId], or registers the component with the ECS */
    public fun getComponentIdForClass(kClass: KClass<*>): GearyComponentId

    /** Allows iteration over all [GearyEntityId]s that match the given family of components. */
    public fun getFamily(family: Family): List<Pair<GearyEntityId, List<GearyComponent>>>

    // Predefined helpers that can be overridden if a faster implementation is possible.

    /** Adds a list of [systems] to the engine. */
    public fun addSystems(vararg systems: TickingSystem) {
        systems.forEach { addSystem(it) }
    }

    /** Adds a list of [components] to entity of [id]. */
    public fun addComponentsFor(id: GearyEntityId, components: Set<GearyComponent>) {
        components.forEach { addComponentFor(id, it) }
    }
}
