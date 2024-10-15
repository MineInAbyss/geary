package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.uuid.UUID2GearyMap

fun Geary.untrackUuidOnRemove(uuid2Geary: UUID2GearyMap) = observe<OnRemove>()
    .involving(query<Uuid>()).exec { (uuid) -> uuid2Geary.remove(uuid) }
