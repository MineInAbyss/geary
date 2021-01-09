
package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.types.GearyEntityType

/**
 * Some extensions may want to represent existing classes as entities in the ECS without having to convert to and from
 * them. For instance, Mobzy implements this class for its custom mobs.
 */
public interface GearyEntity {
    public val gearyId: Int

    public operator fun component1(): Int = gearyId
}

/** The [GearyEntityType] associated with this entity. */
public val GearyEntity.type: GearyEntityType? get() = get<GearyEntityType>()

/** Remove this entity from the ECS. */
public fun GearyEntity.remove() {
    Engine.removeEntity(this)
}

/**
 * A wrapper around an integer id that allows us to use extension functions of [GearyEntity] but gets inlined to avoid
 * performance hits of boxing an integer.
 */
//TODO change name to reflect that it's not boxed anymore.
public inline class BoxedEntityID(override val gearyId: Int) : GearyEntity

/** Gets the entity associated with [id] and runs code on it. */
public inline fun geary(id: Int, run: GearyEntity.() -> Unit): GearyEntity =
        BoxedEntityID(id).apply(run)

/** Gets the entity associated with [id]. */
@Suppress("NOTHING_TO_INLINE")
public inline fun geary(id: Int): GearyEntity = BoxedEntityID(id)
