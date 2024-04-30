package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.configuration.components.ReEmitEvent
import com.mineinabyss.geary.systems.builders.observeWithData

fun GearyModule.reEmitEvent() = observeWithData<ReEmitEvent>().exec {
    entity.getRelationsByKind(event.findByRelationKind.id).forEach { relation ->
        val entity = relation.target.toGeary()
        if (entity.exists()) entity.emit(event = event.findByRelationKind.id, data = event.data)
    }
}
