package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.QueriedEntity
import com.mineinabyss.geary.systems.query.Query

@OptIn(UnsafeAccessors::class)
class NonNullComponentAccessor<T : Any>(
    entity: QueriedEntity,
    id: ComponentId,
) : ComponentAccessor<T>(entity, id) {
    override operator fun get(thisRef: Query): T =
        get(thisRef, beforeRead = {})

    override operator fun set(thisRef: Query, value: T) {
        set(thisRef, value, beforeWrite = {
            if (cachedIndex == -1) {
                entity.entity.set(value, id)
                return
            }
        })
    }
}
