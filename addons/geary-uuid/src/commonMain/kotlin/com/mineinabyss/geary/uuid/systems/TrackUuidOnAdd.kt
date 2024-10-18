package com.mineinabyss.geary.uuid.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.uuid.UUID2GearyMap
import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import kotlin.uuid.Uuid

fun Geary.trackUUIDOnAdd(uuid2Geary: UUID2GearyMap) = observe<OnSet>().involving(query<Uuid>()).exec { (uuid) ->
    val regenerateUUIDOnClash = entity.get<RegenerateUUIDOnClash>()
    if (uuid in uuid2Geary)
        if (regenerateUUIDOnClash != null) {
            val newUuid = Uuid.random()
            entity.set(newUuid)
            uuid2Geary[newUuid] = entity.id
        } else error("Tried tracking entity $entity with already existing uuid $uuid")
    else
        uuid2Geary[uuid] = entity.id
}
