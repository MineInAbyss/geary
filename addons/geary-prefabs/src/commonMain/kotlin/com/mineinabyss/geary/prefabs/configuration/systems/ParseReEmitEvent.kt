package com.mineinabyss.geary.prefabs.configuration.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.prefabs.configuration.components.ReEmitEvent

fun Geary.reEmitEvent() = observeWithData<ReEmitEvent>().exec {
    entity.getRelationsByKind(event.findByRelationKind).forEach { relation ->
        val entity = relation.target.toGeary()
        if (entity.exists()) entity.emit(event = event.dataComponentId, data = event.data)
    }
}
