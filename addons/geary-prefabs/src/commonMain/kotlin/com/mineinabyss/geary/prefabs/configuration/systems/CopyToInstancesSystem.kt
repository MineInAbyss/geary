package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class CopyToInstancesSystem : GearyListener() {
    private val Pointers.baseEntity by whenExtendedEntity()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val copy = baseEntity.get<CopyToInstances>() ?: return
        copy.decodeComponentsTo(target.entity)
    }
}
