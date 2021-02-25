package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.*
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.idofront.plugin.getService
import kotlin.reflect.KClass

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
//TODO I originally made a choice to use ints for al the `for` functions to avoid boxing, but this is likely no longer
// an issue with inline classes. It may be worth just taking the GearyEntity instead.
public interface Engine {
    //TODO relying on spigot here for service
    public companion object : Engine by getService()

    /** Get the next free ID for use with the ECS. */
    public fun getNextId(): GearyEntityId

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: TickingSystem): Boolean

    /** Gets the components for entity of [id]. */
    public fun getComponentsFor(id: GearyEntityId): Set<GearyComponent>

    /** Gets a component of type [T] for entity of [entity]. */
    public fun <T : GearyComponent> getComponentFor(entity: GearyEntityId, component: GearyComponentId): T?

    /**
     * Checks whether entity of [entity] holds a [component type][kClass], without regards for whether or not it's active.
     */
    public fun holdsComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /** Checks whether entity of [entity] has an active [component type][kClass] */
    public fun hasComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /**
     * Removes a [component type][kClass] from entity of [entity].
     *
     * @return Whether the component was present before removal.
     */
    public fun removeComponentFor(entity: GearyEntityId, component: GearyComponentId): Boolean

    /** Adds a [component] of under a [component type][kClass] for entity of [entity].  */
    public fun <T : GearyComponent> addComponentFor(entity: GearyEntityId, component: T): T

    /**
     * Enables a [component type][kClass] for entity of [entity], meaning it will be found via [getComponentFor],
     * [hasComponentFor], or when iterating over a family that includes this component.
     */
    public fun setFor(entity: GearyEntityId, component: GearyComponentId)

    /**
     * Disables a [component type][kClass] for entity of [entity], meaning it will not be found via [getComponentFor],
     * [hasComponentFor], or when iterating over a family that includes this component.
     *
     * However, it will be visible via [holdsComponentFor].
     */
    public fun unsetFor(entity: GearyEntityId, component: GearyComponentId)

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: GearyEntity)

    // Predefined helpers that can be overridden if a faster implementation is possible.

    /** Adds a list of [systems] to the engine. */
    public fun addSystems(vararg systems: TickingSystem) {
        systems.forEach { addSystem(it) }
    }

    /** Adds a list of [components] to entity of [id]. */
    public fun addComponentsFor(id: GearyEntityId, components: Set<GearyComponent>) {
        components.forEach { addComponentFor(id, it) }
    }

    public fun getComponentIdForClass(kClass: KClass<*>): GearyEntityId

    public fun getFamily(
        vararg with: ComponentClass,
        andNot: Array<out ComponentClass> = emptyArray()
    ): List<Pair<GearyEntityId, List<Any>>>
}
