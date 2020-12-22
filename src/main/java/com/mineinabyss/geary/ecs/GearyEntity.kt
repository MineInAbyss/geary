
package com.mineinabyss.geary.ecs

import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.types.GearyEntityType

public interface GearyEntity {
    public val gearyId: Int

    public operator fun component1(): Int = gearyId
}

//TODO pretty sure this won't get the subclasses (i.e. MobType) since we haven't marked reified T with `out`
public val GearyEntity.type: GearyEntityType? get() = get<GearyEntityType>()

public fun GearyEntity.remove() {
    Engine.removeEntity(this)
}

public inline class BoxedEntityID(override val gearyId: Int) : GearyEntity

public inline fun geary(id: Int, run: GearyEntity.() -> Unit): GearyEntity =
        BoxedEntityID(id).apply(run)

@Suppress("NOTHING_TO_INLINE")
public inline fun geary(id: Int): GearyEntity = BoxedEntityID(id)
