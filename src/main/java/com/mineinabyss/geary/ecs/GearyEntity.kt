
package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.engine.Engine

interface GearyEntity {
    val gearyId: Int

    operator fun component1() = gearyId
}

fun GearyEntity.remove() = Engine.removeEntity(this)

class BoxedEntityID(override val gearyId: Int) : GearyEntity

inline fun geary(id: Int, run: GearyEntity.() -> Unit): GearyEntity =
        BoxedEntityID(id).apply(run)

@Suppress("NOTHING_TO_INLINE")
inline fun geary(id: Int): GearyEntity = BoxedEntityID(id)