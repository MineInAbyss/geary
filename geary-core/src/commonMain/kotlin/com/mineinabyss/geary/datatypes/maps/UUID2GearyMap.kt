package com.mineinabyss.geary.datatypes.maps

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.RegenerateUUIDOnClash
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope

public class UUID2GearyMap(
    override val engine: Engine
) : GearyListener() {
    private val uuid2geary = mutableMapOf<Uuid, Long>()

    public operator fun get(uuid: Uuid): GearyEntity? =
        uuid2geary[uuid]?.toGeary()

    public operator fun set(uuid: Uuid, entity: GearyEntity): GearyEntity? =
        uuid2geary.put(uuid, entity.id.toLong())?.toGeary()

    public operator fun contains(uuid: Uuid): Boolean = uuid2geary.containsKey(uuid)

    public fun remove(uuid: Uuid): GearyEntity? =
        uuid2geary.remove(uuid)?.toGeary()

    public fun startTracking() {
        engine.addSystem(TrackUuidOnAdd())
        engine.addSystem(UnTrackUuidOnRemove())
    }

    public inner class TrackUuidOnAdd : GearyListener() {
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

    public inner class UnTrackUuidOnRemove : GearyListener() {
        private val TargetScope.uuid by get<Uuid>().onTarget()
        private val EventScope.removed by family { has<EntityRemoved>() }.onEvent()

        @Handler
        private fun TargetScope.untrack() {
            remove(uuid)
        }
    }
}
