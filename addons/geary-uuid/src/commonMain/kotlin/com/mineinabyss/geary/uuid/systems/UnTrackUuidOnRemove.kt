package com.mineinabyss.geary.uuid.systems

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.geary.uuid.uuid2Geary

//TODO fix
fun createUntrackUuidOnRemoveSystem() = geary.listener(object : ListenerQuery() {
    val uuid by target.get<Uuid>()
}.apply { event.match { has<EntityRemoved>() } }) {
    uuid2Geary.remove(uuid)
}
