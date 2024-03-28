package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun GearyModule.createCopyToInstancesSystem() = listener(object : ListenerQuery() {
    val baseEntity by event.extendedEntity()
}).exec {
    val copy = baseEntity.get<CopyToInstances>() ?: return@exec
    copy.decodeComponentsTo(entity)
}
