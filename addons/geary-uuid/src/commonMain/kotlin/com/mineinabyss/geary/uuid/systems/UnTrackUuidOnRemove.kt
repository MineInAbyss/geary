package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.EventQuery
import com.mineinabyss.geary.systems.system
import com.mineinabyss.geary.uuid.uuid2Geary

//TODO fix
fun untrackUuidOnRemove() = geary.system(object : EventQuery() {
    val uuid by target.get<Uuid>()
//    val removed by event.family { has<EntityRemoved>() }
}) {
    onTick {
        uuid2Geary.remove(uuid)
    }
}
