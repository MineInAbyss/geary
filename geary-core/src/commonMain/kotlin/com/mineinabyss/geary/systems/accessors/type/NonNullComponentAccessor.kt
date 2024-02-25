package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.query.QueriedEntity
import com.mineinabyss.geary.systems.query.Query

class NonNullComponentAccessor<T : Any>(
    cacheArchetypeInfo: Boolean,
    originalAccessor: Accessor?,
    entity: QueriedEntity,
    id: ComponentId,
) : ComponentAccessor<T>(cacheArchetypeInfo, originalAccessor, entity, id) {
    override fun get(thisRef: Query): T =
        get(thisRef, beforeRead = {})

    override fun set(query: Query, value: T) {
        set(query, value, beforeWrite = {})
    }
}
