package com.mineinabyss.geary.minecraft.access

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.entities.addPrefab
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.minecraft.events.GearyEntityRemoveEvent
import com.mineinabyss.geary.minecraft.events.GearyMinecraftLoadEvent
import com.mineinabyss.geary.minecraft.hasComponentsEncoded
import com.mineinabyss.geary.minecraft.store.decodeComponentsFrom
import com.mineinabyss.geary.minecraft.store.encodeComponents
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.nms.aliases.BukkitEntity
import com.mineinabyss.idofront.nms.entity.typeName
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import java.util.*
import kotlin.collections.set

internal typealias OnEntityRegister = MutableList<GearyComponent>.(Entity) -> Unit
internal typealias OnEntityUnregister = (GearyEntity, Entity) -> Unit

public object BukkitEntityAccess : Listener {
    private val entityMap = Object2LongOpenHashMap<UUID>()
    private fun <T> Object2LongOpenHashMap<T>.getOrNull(key: T): Long? = getOrDefault(key, null)

    //TODO this should be done through events
    private val onBukkitEntityRegister: MutableList<OnEntityRegister> = mutableListOf()
    private val onBukkitEntityUnregister: MutableList<OnEntityUnregister> = mutableListOf()

    public fun onBukkitEntityRegister(add: OnEntityRegister) {
        onBukkitEntityRegister.add(add)
    }

    public fun onBukkitEntityUnregister(add: OnEntityUnregister) {
        onBukkitEntityUnregister.add(add)
    }


    public fun <T : Entity> registerEntity(entity: T, attachTo: GearyEntity? = null): GearyEntity {
        //if the entity is already registered, return it
        entityMap.getOrNull(entity.uniqueId)?.let { return geary(it) }

        val gearyEntity: GearyEntity = attachTo ?: Engine.entity {}
        gearyEntity.apply {
            // allow us to both get the BukkitEntity and specific class (ex Player)
            set<BukkitEntity>(entity)
            entity.type.entityClass?.kotlin?.let { bukkitClass ->
                set(entity, bukkitClass)
            }

            setAll(
                onBukkitEntityRegister.flatMap { mapping ->
                    mutableListOf<GearyComponent>().apply { mapping(entity) }
                }
            )
        }

        //TODO extension function to get prefab key from entity, including correct namespace
        PrefabManager[PrefabKey("mobzy", entity.typeName)]?.let {
            gearyEntity.addPrefab(it)
        }

        val pdc = entity.persistentDataContainer
        if (pdc.hasComponentsEncoded)
            gearyEntity.decodeComponentsFrom(pdc)

        entityMap[entity.uniqueId] = gearyEntity.id.toLong()

        GearyMinecraftLoadEvent(gearyEntity).call()

        return gearyEntity
    }

    private fun unregisterEntity(entity: Entity): GearyEntity? {
        val gearyEntity = geary(entityMap.getOrNull(entity.uniqueId) ?: return null)

        for (extension in onBukkitEntityUnregister) {
            extension(gearyEntity, entity)
        }
        if (!entityMap.containsKey(entity.uniqueId)) return null
        return geary(entityMap.removeLong(entity.uniqueId))
    }

    public fun getEntityOrNull(entity: Entity): GearyEntity? = entityMap.getOrNull(entity.uniqueId)?.let { geary(it) }

    public fun <T : Entity> getEntity(entity: T): GearyEntity =
        getEntityOrNull(entity) ?: registerEntity(entity)

    @EventHandler(priority = EventPriority.LOWEST)
    public fun GearyEntityRemoveEvent.onEntityRemoved() {
        entity.toBukkit<Entity>()?.let {
            unregisterEntity(it)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerLoginEvent.onPlayerLogin() {
        registerEntity(player)
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST)
    public fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        val gearyEntity = getEntityOrNull(entity) ?: return
        //TODO some way of knowing if this entity is permanently removed
        entity.encodeComponents(gearyEntity)
        unregisterEntity(entity)
        gearyEntity.removeEntity()
    }

    //TODO Is there anything we'd actually want to do with the ECS while the player sees their respawn screen?
    // If so, dont remove them on entity remove event and just refresh the inventory here
    /** Player death counts as an entity being removed from the world so we should add them back after respawn */
    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerPostRespawnEvent.onPlayerRespawn() {
        registerEntity(player)
    }
}
