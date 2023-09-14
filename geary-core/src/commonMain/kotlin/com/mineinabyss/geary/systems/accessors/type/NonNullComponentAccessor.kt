package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.accessors.Pointer

@OptIn(UnsafeAccessors::class)
class NonNullComponentAccessor<T : Any>(
    id: ComponentId,
) : ComponentAccessor<T>(id) {
    override operator fun get(thisRef: Pointer): T =
        get(thisRef, beforeRead = {})

    override operator fun set(thisRef: Pointer, value: T) {
        set(thisRef, value, beforeWrite = {
            if (cachedIndex == -1) {
                thisRef.entity.set(value, id)
                return
            }
        })
    }
}
