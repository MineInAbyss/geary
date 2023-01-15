package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.uuid.uuid2Geary

class UnTrackUuidOnRemove : GearyListener() {
    private val TargetScope.uuid by get<Uuid>().onTarget()
    private val EventScope.removed by family { has<EntityRemoved>() }.onEvent()

    @Handler
    private fun TargetScope.untrack() {
        uuid2Geary.remove(uuid)
    }
}
