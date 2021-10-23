package com.mineinabyss.geary.minecraft.access

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.events.GearyEntityRemoveEvent
import com.mineinabyss.geary.minecraft.hasComponentsEncoded
import com.mineinabyss.geary.minecraft.store.decodeComponentsFrom
import com.mineinabyss.geary.minecraft.store.encodeComponentsTo
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

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
            setAll(
                onBukkitEntityRegister.flatMap { mapping ->
                    mutableListOf<GearyComponent>().apply { mapping(entity) }
                }
            )

            BukkitAssociations.register(uuid, gearyEntity)

            val pdc = entity.persistentDataContainer
            if (pdc.hasComponentsEncoded)
                gearyEntity.decodeComponentsFrom(pdc)
            // allow us to both get the BukkitEntity and specific class (ex Player)
            entity.type.entityClass?.kotlin?.let { bukkitClass ->
                set(entity, bukkitClass)
            }

            // Any component addition listener that wants Bukkit entity will immediately
            // run, so we do this last. Potentially change how that fires in the future.
            set(entity)
        }

        return gearyEntity
    }

    private fun unregisterEntity(entity: Entity): GearyEntity? {
        val gearyEntity = BukkitAssociations[entity.uniqueId] ?: return null

        for (extension in onBukkitEntityUnregister) {
            extension(gearyEntity, entity)
        }

        val pdc = entity.persistentDataContainer
        gearyEntity.encodeComponentsTo(pdc)

        return BukkitAssociations.remove(entity.uniqueId)
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public fun GearyEntityRemoveEvent.onEntityRemoved() {
        entity.get<BukkitEntity>()?.let {
            unregisterEntity(it)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerJoinEvent.onPlayerLogin() {
        registerEntity(player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerQuitEvent.onPlayerLogout() {
        removeEntityAndEncodeComponents(player)
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.LOWEST)
    public fun EntityAddToWorldEvent.onBukkitEntityAdd() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        registerEntity(entity)
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST)
    public fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        removeEntityAndEncodeComponents(entity)
    }

    public fun removeEntityAndEncodeComponents(entity: BukkitEntity) {
        val gearyEntity = entity.toGearyOrNull() ?: return
        //TODO some way of knowing if this entity is permanently removed
        gearyEntity.encodeComponentsTo(entity)
        unregisterEntity(entity)
        gearyEntity.removeEntity()
    }

    /** Player death counts as an entity being removed from the world so we should add them back after respawn */
    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerPostRespawnEvent.onPlayerRespawn() {
        //TODO add Dead component on death and remove it here
        // Or should we add an inactive component that prevents systems from iterating over dead players?
    }
}
