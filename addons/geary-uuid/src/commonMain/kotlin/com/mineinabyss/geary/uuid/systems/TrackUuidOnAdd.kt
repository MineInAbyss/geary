package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import com.mineinabyss.geary.uuid.uuid2Geary

@OptIn(UnsafeAccessors::class)
fun createTrackUUIDOnAddSystem() = geary.listener(object : ListenerQuery() {
    var uuid by get<Uuid>()
    val regenerateUUIDOnClash by get<RegenerateUUIDOnClash>().orNull()
    override fun ensure() = event.anySet(::uuid)
}).exec {
    if (uuid in uuid2Geary)
        if (regenerateUUIDOnClash != null) {
            val newUuid = uuid4()
            uuid = newUuid
            uuid2Geary[newUuid] = entity
        } else error("Tried tracking entity $entity with already existing uuid $uuid")
    else
        uuid2Geary[uuid] = entity
}
