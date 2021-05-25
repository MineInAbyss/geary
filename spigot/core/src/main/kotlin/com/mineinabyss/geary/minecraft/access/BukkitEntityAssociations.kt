package com.mineinabyss.geary.minecraft.access

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
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
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

internal typealias OnEntityRegister = GearyEntity.(Entity) -> Unit
internal typealias OnEntityUnregister = GearyEntity.(Entity) -> Unit

public object BukkitEntityAssociations : Listener {
    //TODO this should be done through events
    private val onBukkitEntityRegister: MutableList<OnEntityRegister> = mutableListOf()
    private val onBukkitEntityUnregister: MutableList<OnEntityUnregister> = mutableListOf()

    public fun onBukkitEntityRegister(add: OnEntityRegister) {
        onBukkitEntityRegister.add(add)
    }

    public fun onBukkitEntityUnregister(add: OnEntityUnregister) {
        onBukkitEntityUnregister.add(add)
    }

    public fun registerEntity(entity: Entity): GearyEntity {
        val uuid = entity.uniqueId
        BukkitAssociations[uuid]?.let { return it }

        val gearyEntity = Engine.entity()

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

        BukkitAssociations.register(uuid, gearyEntity)

        return gearyEntity
    }

    private fun unregisterEntity(entity: Entity): GearyEntity? {
        val gearyEntity = BukkitAssociations.get(entity.uniqueId) ?: return null

        for (extension in onBukkitEntityUnregister) {
            extension(gearyEntity, entity)
        }

        if (entity.uniqueId !in BukkitAssociations) return null
        return BukkitAssociations.remove(entity.uniqueId)
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public fun GearyEntityRemoveEvent.onEntityRemoved() {
        entity.get<BukkitEntity>()?.let {
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
        val gearyEntity = gearyOrNull(entity) ?: return
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
