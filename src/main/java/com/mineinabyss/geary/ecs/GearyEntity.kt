
package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.types.GearyEntityType

public interface GearyEntity {
    public val gearyId: Int

    public operator fun component1(): Int = gearyId
}

public val GearyEntity.type: GearyEntityType? get() = get<GearyEntityType>()

public fun GearyEntity.remove() {
    Engine.removeEntity(this)
}

/**
 * A wrapper around an integer id that allows us to use extension functions of [GearyEntity] but gets inlined to avoid
 * performance hits of boxing an integer.
 */
//TODO change name to reflect that it's not boxed anymore.
public inline class BoxedEntityID(override val gearyId: Int) : GearyEntity

public inline fun geary(id: Int, run: GearyEntity.() -> Unit): GearyEntity =
        BoxedEntityID(id).apply(run)

@Suppress("NOTHING_TO_INLINE")
public inline fun geary(id: Int): GearyEntity = BoxedEntityID(id)
