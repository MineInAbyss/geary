package com.mineinabyss.geary.ecs.entities

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.EntityRemoved

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
        private val TargetScope.uuid by added<Uuid>().onTarget()

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

        override fun onStart() {
            event.has<EntityRemoved>()
        }

        @Handler
        private fun TargetScope.untrack() {
            remove(uuid)
        }
    }
}
