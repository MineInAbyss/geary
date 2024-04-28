package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.uuid.uuid2Geary

fun GearyModule.untrackUuidOnRemove() = observe<OnRemove>()
    .involving(query<Uuid>()).exec { (uuid) -> uuid2Geary.remove(uuid) }

