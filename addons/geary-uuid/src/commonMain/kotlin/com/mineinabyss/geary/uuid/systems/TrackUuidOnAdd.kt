package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import com.mineinabyss.geary.uuid.uuid2Geary

fun GearyModule.trackUUIDOnAdd() = observe<OnSet>().involving(query<Uuid>()).exec { (uuid) ->
    val regenerateUUIDOnClash = entity.get<RegenerateUUIDOnClash>()
    if (uuid in uuid2Geary)
        if (regenerateUUIDOnClash != null) {
            val newUuid = uuid4()
            entity.set(newUuid)
            uuid2Geary[newUuid] = entity
        } else error("Tried tracking entity $entity with already existing uuid $uuid")
    else
        uuid2Geary[uuid] = entity
}
