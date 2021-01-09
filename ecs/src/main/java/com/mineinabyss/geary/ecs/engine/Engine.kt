package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.components.Conditions
import com.mineinabyss.geary.ecs.geary
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
    public fun getNextId(): Int

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine */
    public fun addSystem(system: TickingSystem): Boolean

    /** Gets the components for entity of [id]. */
    public fun getComponentsFor(id: Int): Set<GearyComponent>

    /** Gets a component of type [T] for entity of [id]. */
    public fun <T : GearyComponent> getComponentFor(kClass: KClass<T>, id: Int): T?

    /**
     * Checks whether entity of [id] holds a [component type][kClass], without regards for whether or not it's active.
     */
    public fun holdsComponentFor(kClass: ComponentClass, id: Int): Boolean

    /** Checks whether entity of [id] has an active [component type][kClass] */
    public fun hasComponentFor(kClass: ComponentClass, id: Int): Boolean

    /**
     * Removes a [component type][kClass] from entity of [id].
     *
     * @return Whether the component was present before removal.
     */
    public fun removeComponentFor(kClass: ComponentClass, id: Int): Boolean

    /** Adds a [component] of under a [component type][kClass] for entity of [id].  */
    public fun <T : GearyComponent> addComponentFor(kClass: KClass<out T>, id: Int, component: T): T

    /**
     * Enables a [component type][kClass] for entity of [id], meaning it will be found via [getComponentFor],
     * [hasComponentFor], or when iterating over a family that includes this component.
     */
    public fun enableComponentFor(kClass: ComponentClass, id: Int)

    /**
     * Disables a [component type][kClass] for entity of [id], meaning it will not be found via [getComponentFor],
     * [hasComponentFor], or when iterating over a family that includes this component.
     *
     * However, it will be visible via [holdsComponentFor].
     */
    public fun disableComponentFor(kClass: ComponentClass, id: Int)

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
        vararg components: ComponentClass,
        andNot: Array<out ComponentClass> = emptyArray(),
        checkConditions: Boolean = true
    ): BitVector

    // Predefined helpers that can be overridden if a faster implementation is possible.

    /** Adds a list of [systems] to the engine. */
    public fun addSystems(vararg systems: TickingSystem) {
        systems.forEach { addSystem(it) }
    }

    /** Adds a list of [components] to entity of [id]. */
    public fun addComponentsFor(id: Int, components: Set<GearyComponent>) {
        components.forEach { addComponentFor(it::class, id, it) }
    }
}

public inline fun Engine.entity(run: GearyEntity.() -> Unit): GearyEntity = geary(getNextId(), run)
