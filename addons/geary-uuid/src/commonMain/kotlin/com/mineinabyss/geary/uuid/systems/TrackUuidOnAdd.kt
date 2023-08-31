package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.systems.GearyListener

import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import com.mineinabyss.geary.uuid.uuid2Geary

class TrackUuidOnAdd : GearyListener() {
    private val TargetScope.uuid by onSet<Uuid>().onTarget()

    @Handler
    private fun TargetScope.track() {
        if (uuid in uuid2Geary)
            if (entity.has<RegenerateUUIDOnClash>()) {
                val newUuid = uuid4()
                entity.set(newUuid)
                uuid2Geary[newUuid] = entity
            } else error("Tried tracking entity $entity with already existing uuid $uuid")
        else
            uuid2Geary[uuid] = entity
    }
}

