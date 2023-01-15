package com.mineinabyss.geary.uuid

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.toGeary

class UUID2GearyMap {
    private val map = mutableMapOf<Uuid, Long>()

    operator fun get(uuid: Uuid): GearyEntity? =
        map[uuid]?.toGeary()

    operator fun set(uuid: Uuid, entity: GearyEntity): GearyEntity? =
        map.put(uuid, entity.id.toLong())?.toGeary()

    operator fun contains(uuid: Uuid): Boolean = map.containsKey(uuid)

    fun remove(uuid: Uuid): GearyEntity? =
        map.remove(uuid)?.toGeary()
}
