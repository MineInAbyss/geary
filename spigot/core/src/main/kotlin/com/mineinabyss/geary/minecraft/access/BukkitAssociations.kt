package com.mineinabyss.geary.minecraft.access

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.minecraft.events.GearyEntityRemoveEvent
import com.mineinabyss.geary.minecraft.events.GearyMinecraftLoadEvent
import com.mineinabyss.idofront.events.call
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.*
import kotlin.collections.set

public object BukkitAssociations : Listener {
    private val entityMap = Object2LongOpenHashMap<UUID>()

    public operator fun get(uuid: UUID): GearyEntity? {
        return geary(entityMap.getOrDefault(uuid, null) ?: return null)
    }

    public operator fun contains(uuid: UUID): Boolean = entityMap.contains(uuid)

    public fun register(uuid: UUID, entity: GearyEntity) {
        entityMap[uuid] = entity.id.toLong()
        GearyMinecraftLoadEvent(entity).call()
    }

    public fun remove(uuid: UUID): GearyEntity? {
        if (uuid !in entityMap) return null
        return geary(entityMap.removeLong(uuid))
    }

    public inline fun getOrPut(uuid: UUID, run: () -> GearyEntity): GearyEntity {
        //if the entity is already registered, return it
        get(uuid)?.let { return it }
        val entity = run()
        register(uuid, entity)
        return entity
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public fun GearyEntityRemoveEvent.onEntityRemoved() {
        entity.get<UUID>()?.let { remove(it) }
    }
}
