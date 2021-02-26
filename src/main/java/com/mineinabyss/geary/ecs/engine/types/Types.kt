package com.mineinabyss.geary.ecs.engine.types

import com.mineinabyss.geary.ecs.GearyComponentId
import com.mineinabyss.geary.ecs.GearyEntityId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.GearyEngine
import java.util.*

public typealias GearyType = SortedSet<GearyComponentId>

internal class GearyTypeMap {
    private val typeMap = mutableMapOf<GearyEntityId, GearyType>()

    operator fun get(id: GearyEntityId): GearyType {
        return typeMap.getOrPut(id, { sortedSetOf() })
    }

    fun set(entity: GearyEntityId, componentId: GearyComponentId) =
        get(entity).add(componentId)

    fun unset(entity: GearyEntityId, componentId: GearyComponentId) =
        get(entity).remove(componentId)

    fun has(entity: GearyEntityId, component: GearyComponentId) =
        get(entity).contains(component)

    fun remove(entity: GearyEntityId) = typeMap.remove(entity)
}

public val GearyEntity.type: GearyType get() = (Engine as GearyEngine).typeMap[id]
