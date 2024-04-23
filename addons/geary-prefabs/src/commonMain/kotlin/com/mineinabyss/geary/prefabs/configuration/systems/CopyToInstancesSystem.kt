package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.events.types.OnExtend
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.builders.observeWithData

fun GearyModule.createCopyToInstancesSystem() = observeWithData<OnExtend>()
    .exec {
        val copy = event.baseEntity.get<CopyToInstances>() ?: return@exec
        copy.decodeComponentsTo(entity)
    }
