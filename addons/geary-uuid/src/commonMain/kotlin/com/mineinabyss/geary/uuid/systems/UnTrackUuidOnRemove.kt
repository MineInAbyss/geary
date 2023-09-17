package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

import com.mineinabyss.geary.uuid.uuid2Geary

class UnTrackUuidOnRemove : GearyListener() {
    private val Pointers.uuid by get<Uuid>().on(target)
    private val Pointers.removed by family { has<EntityRemoved>() }.on(event)

    override fun Pointers.handle() {
        uuid2Geary.remove(uuid)
    }
}
