package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.accessors.Pointer

@OptIn(UnsafeAccessors::class)
class RemovableComponentAccessor<T>(
    id: ComponentId,
) : ComponentAccessor<T?>(id) {
    override fun get(thisRef: Pointer): T? =
        get(thisRef, beforeRead = {
            if (cachedIndex == -1) return null
        })

    override fun set(thisRef: Pointer, value: T?) =
        set(thisRef, value, beforeWrite = {
            if (cachedIndex == -1) {
                if (value == null) return
                else thisRef.entity.set(value, id)
                return
            }
            if (value == null) {
                thisRef.entity.remove(id)
                return
            }
        })
}
