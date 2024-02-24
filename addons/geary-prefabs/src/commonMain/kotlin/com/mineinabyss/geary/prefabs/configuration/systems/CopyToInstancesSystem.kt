package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

@OptIn(UnsafeAccessors::class)
fun createCopyToInstancesSystem() = geary.listener(object : ListenerQuery() {
    val baseEntity by event.extendedEntity()
}).exec {
    val copy = baseEntity.get<CopyToInstances>() ?: return@exec
    copy.decodeComponentsTo(entity)
}
