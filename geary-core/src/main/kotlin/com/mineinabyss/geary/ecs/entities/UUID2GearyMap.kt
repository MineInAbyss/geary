package com.mineinabyss.geary.ecs.entities

import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.EntityRemoved
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import java.util.*

public class UUID2GearyMap : GearyListener() {
    private val uuid2geary = Object2LongOpenHashMap<UUID>().apply {
        defaultReturnValue(-1)
    }

    public operator fun get(uuid: UUID): GearyEntity? =
        uuid2geary.getLong(uuid).takeIf { it != -1L }?.toGeary()

    public operator fun set(uuid: UUID, entity: GearyEntity): GearyEntity =
        uuid2geary.put(uuid, entity.id.toLong()).toGeary()

    public operator fun contains(uuid: UUID): Boolean = uuid2geary.containsKey(uuid)

    public fun remove(uuid: UUID): GearyEntity? =
        uuid2geary.removeLong(uuid).takeIf { it != -1L }?.toGeary()

    public fun startTracking() {
        engine.addSystem(TrackUUIDOnAdd())
        engine.addSystem(UnTrackUUIDOnRemove())
    }

    public inner class TrackUUIDOnAdd : GearyListener() {
        private val TargetScope.uuid by added<UUID>()

        @Handler
        private fun TargetScope.track() {
            if (contains(uuid))
                if (entity.has<RegenerateUUIDOnClash>()) {
                    val newUUID = UUID.randomUUID()
                    entity.set(newUUID)
                    set(newUUID, entity)
                } else error("Tried tracking entity $entity with already existing uuid $uuid")
            else
                set(uuid, entity)
        }
    }

    public inner class UnTrackUUIDOnRemove : GearyListener() {
        private val TargetScope.uuid by get<UUID>()

        override fun onStart() {
            event.has<EntityRemoved>()
        }

        @Handler
        private fun TargetScope.untrack() {
            remove(uuid)
        }
    }
}
