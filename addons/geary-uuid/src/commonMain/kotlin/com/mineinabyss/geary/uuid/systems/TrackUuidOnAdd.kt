package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import com.mineinabyss.geary.uuid.uuid2Geary

class TrackUuidOnAdd : GearyListener() {
    var Pointers.uuid by get<Uuid>().whenSetOnTarget()
    val Pointers.regenerateUUIDOnClash by get<RegenerateUUIDOnClash>().orNull().on(target)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        if (uuid in uuid2Geary)
            if (regenerateUUIDOnClash != null) {
                val newUuid = uuid4()
                uuid = newUuid
                uuid2Geary[newUuid] = target.entity
            } else error("Tried tracking entity $target.entity with already existing uuid $uuid")
        else
            uuid2Geary[uuid] = target.entity
    }
}

