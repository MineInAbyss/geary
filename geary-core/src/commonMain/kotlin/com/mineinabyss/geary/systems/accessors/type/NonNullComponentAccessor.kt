package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.query.QueriedEntity

@OptIn(UnsafeAccessors::class)
class NonNullComponentAccessor<T : Any>(
    entity: QueriedEntity,
    id: ComponentId,
) : ComponentAccessor<T>(entity, id) {
    override operator fun get(thisRef: AccessorOperations): T =
        get(thisRef, beforeRead = {})

    override operator fun set(thisRef: AccessorOperations, value: T) {
        set(thisRef, value, beforeWrite = {
            if (cachedIndex == -1) {
                queriedEntity.entity.set(value, id)
                return
            }
        })
    }
}
