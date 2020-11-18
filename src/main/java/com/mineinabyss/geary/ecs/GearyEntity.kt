
package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.engine.Engine

public interface GearyEntity {
    public val gearyId: Int

    public operator fun component1(): Int = gearyId
}

public fun GearyEntity.remove() {
    Engine.removeEntity(this)
}

public class BoxedEntityID(override val gearyId: Int) : GearyEntity

public inline fun geary(id: Int, run: GearyEntity.() -> Unit): GearyEntity =
        BoxedEntityID(id).apply(run)

@Suppress("NOTHING_TO_INLINE")
public inline fun geary(id: Int): GearyEntity = BoxedEntityID(id)
