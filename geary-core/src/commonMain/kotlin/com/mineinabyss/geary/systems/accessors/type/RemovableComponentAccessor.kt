package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.query.QueriedEntity

class RemovableComponentAccessor<T>(
    entity: QueriedEntity,
    id: ComponentId,
) : ComponentAccessor<T?>(entity, id) {
    override fun get(thisRef: AccessorOperations): T? =
        get(thisRef, beforeRead = {
            if (cachedIndex == -1) return null
        })

    @OptIn(UnsafeAccessors::class)
    override fun set(thisRef: AccessorOperations, value: T?) =
        set(thisRef, value, beforeWrite = {
            if (cachedIndex == -1) {
                if (value == null) return
                else queriedEntity.entity.set(value, id)
                return
            }
            if (value == null) {
                queriedEntity.entity.remove(id)
                return
            }
        })
}
