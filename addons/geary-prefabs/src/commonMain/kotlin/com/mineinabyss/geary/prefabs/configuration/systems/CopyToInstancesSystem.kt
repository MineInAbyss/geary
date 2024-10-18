package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.observers.events.OnExtend
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances

fun Geary.createCopyToInstancesSystem() = observeWithData<OnExtend>()
    .exec {
        val copy = event.baseEntity.toGeary().get<CopyToInstances>() ?: return@exec
        copy.decodeComponentsTo(entity)
    }
