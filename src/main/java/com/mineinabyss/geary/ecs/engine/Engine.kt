package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.*
import com.mineinabyss.geary.ecs.actions.components.Conditions
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.idofront.plugin.getService
import net.onedaybeard.bitvector.BitVector
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

    /** Gets a component of type [T] for entity of [id]. */
    public fun <T : GearyComponent> getComponentFor(component: GearyComponentId, id: GearyEntityId): T?

    /**
     * Checks whether entity of [id] holds a [component type][kClass], without regards for whether or not it's active.
     */
    public fun holdsComponentFor(component: GearyComponentId, id: GearyEntityId): Boolean

    /** Checks whether entity of [id] has an active [component type][kClass] */
    public fun hasComponentFor(component: GearyComponentId, id: GearyEntityId): Boolean

    /**
     * Removes a [component type][kClass] from entity of [id].
     *
     * @return Whether the component was present before removal.
     */
    public fun removeComponentFor(component: GearyComponentId, id: GearyEntityId): Boolean

    /** Adds a [component] of under a [component type][kClass] for entity of [id].  */
    public fun <T : GearyComponent> addComponentFor(id: GearyEntityId, component: T): T

    /** Adds a [component] of under a [component type][kClass] for entity of [id].  */
    public fun addEntityFor(id: GearyEntityId, componentId: GearyComponentId)

    /**
     * Enables a [component type][kClass] for entity of [id], meaning it will be found via [getComponentFor],
     * [hasComponentFor], or when iterating over a family that includes this component.
     */
    public fun enableComponentFor(component: GearyComponentId, id: GearyEntityId)

    /**
     * Disables a [component type][kClass] for entity of [id], meaning it will not be found via [getComponentFor],
     * [hasComponentFor], or when iterating over a family that includes this component.
     *
     * However, it will be visible via [holdsComponentFor].
     */
    public fun disableComponentFor(component: GearyComponentId, id: GearyEntityId)

    /** Removes an entity from the ECS, freeing up its entity id. */
    public fun removeEntity(entity: GearyEntity)

    /**
     * Gets a bitset of all entities matching all of the defined [components] and not [andNot].
     *
     * @param checkConditions Whether to perform additional checks defined by each component within the [Conditions]
     * component if it is present on this entity.
     */
    //TODO this shouldn't be in interface but currently required for inline functions in [Iteration]
    public fun getBitsMatching(
        vararg components: GearyComponentId,
        andNot: IntArray = IntArray(0),
        checkConditions: Boolean = true
    ): BitVector

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
}

public inline fun Engine.entity(run: GearyEntity.() -> Unit): GearyEntity = geary(getNextId(), run)

public inline fun <reified T> componentId(): GearyEntityId = componentId(T::class)

public fun componentId(component: GearyComponent): GearyEntityId = componentId(component::class)

public fun componentId(kClass: KClass<*>): GearyEntityId = Engine.getComponentIdForClass(kClass)

