package com.mineinabyss.geary.minecraft.access

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import java.util.*
import kotlin.collections.set

public object BukkitAssociations {
    private val entityMap = Object2LongOpenHashMap<UUID>()

    public fun getOrNull(uuid: UUID): GearyEntity? {
        return geary(entityMap.getOrDefault(uuid, null) ?: return null)
    }
    
    public operator fun contains(uuid: UUID): Boolean = entityMap.contains(uuid)

    public operator fun set(uuid: UUID, entity: GearyEntity) {
        entityMap[uuid] = entity.id.toLong()
    }
    
    public fun remove(uuid: UUID): GearyEntity = geary(entityMap.removeLong(uuid))
    
    public fun register(uuid: UUID, attachTo: GearyEntity? = null): GearyEntity {
        //if the entity is already registered, return it
        getOrNull(uuid)?.let { return it }

        return attachTo ?: Engine.entity {}
    }
}
