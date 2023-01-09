package com.mineinabyss.geary.uuid

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope

class UUID2GearyMap : GearyListener() {
    private val uuid2geary = mutableMapOf<Uuid, Long>()

    fun track() {
        geary.pipeline.addSystem(TrackUuidOnAdd())
        geary.pipeline.addSystem(UnTrackUuidOnRemove())
    }

    operator fun get(uuid: Uuid): GearyEntity? =
        uuid2geary[uuid]?.toGeary()

    operator fun set(uuid: Uuid, entity: GearyEntity): GearyEntity? =
        uuid2geary.put(uuid, entity.id.toLong())?.toGeary()

    operator fun contains(uuid: Uuid): Boolean = uuid2geary.containsKey(uuid)

    fun remove(uuid: Uuid): GearyEntity? =
        uuid2geary.remove(uuid)?.toGeary()

    inner class TrackUuidOnAdd : GearyListener() {
        private val TargetScope.uuid by onSet<Uuid>().onTarget()

        @Handler
        private fun TargetScope.track() {
            if (contains(uuid))
                if (entity.has<RegenerateUUIDOnClash>()) {
                    val newUuid = uuid4()
                    entity.set(newUuid)
                    set(newUuid, entity)
                } else error("Tried tracking entity $entity with already existing uuid $uuid")
            else
                set(uuid, entity)
        }
    }

    inner class UnTrackUuidOnRemove : GearyListener() {
        private val TargetScope.uuid by get<Uuid>().onTarget()
        private val EventScope.removed by family { has<EntityRemoved>() }.onEvent()

        @Handler
        private fun TargetScope.untrack() {
            remove(uuid)
        }
    }
}
