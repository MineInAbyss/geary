package com.mineinabyss.geary.papermc.access

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.systems
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.GearyMCContext
import com.mineinabyss.geary.papermc.GearyMCContextKoin
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.geary.papermc.store.hasComponentsEncoded
import com.mineinabyss.geary.papermc.store.loadComponentsFrom
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.typealiases.BukkitEntity
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.collections.set

class BukkitEntity2Geary : Listener, GearyMCContext by GearyMCContextKoin() {
    private val entityMap = Int2LongOpenHashMap().apply { defaultReturnValue(-1) }

    fun startTracking() {
        geary.listeners(this)
        systems(Register(), Unregister())
    }

    operator fun get(entityId: Int): GearyEntity? {
        val id = entityMap.get(entityId)
        if (id == -1L) return null
        return id.toGeary()
    }

    operator fun set(bukkit: BukkitEntity, entity: GearyEntity) {
        entityMap[bukkit.entityId] = entity.id.toLong()
    }

    operator fun contains(entityId: Int): Boolean = entityMap.containsKey(entityId)

    fun remove(entityId: Int): GearyEntity? {
        return entityMap.remove(entityId).takeIf { it != -1L }?.toGeary()
    }

    private inner class Register : GearyListener() {
        val TargetScope.bukkit by onSet<BukkitEntity>()

        @Handler
        fun TargetScope.loadPersisted() {
            set(bukkit, entity)

            val pdc = bukkit.persistentDataContainer
            if (pdc.hasComponentsEncoded)
                entity.loadComponentsFrom(pdc)
            // allow us to both get the BukkitEntity and specific class (ex Player)
            bukkit.type.entityClass?.kotlin?.let { bukkitClass ->
                entity.set(bukkit, bukkitClass)
            }

            entity.set(bukkit.uniqueId)
        }
    }

    private inner class Unregister : GearyListener() {
        val TargetScope.bukkit by get<BukkitEntity>()
        val EventScope.removed by family { has<EntityRemoved>() }

        @Handler
        fun TargetScope.persistComponents() {
            remove(bukkit.entityId)
            entity.encodeComponentsTo(bukkit)
        }
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityAddToWorldEvent.onBukkitEntityAdd() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        entity.toGeary()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.onPlayerLogin() {
        player.toGeary()
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        // We remove the geary entity one tick after the Bukkit one has been removed to ensure nothing
        // else that tries to access the geary entity from Bukkit will create a new entity.
        entity.toGearyOrNull()?.removeEntity()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onPlayerLogout() {
        player.toGearyOrNull()?.removeEntity()
    }

    /** Player death counts as an entity being removed from the world so we should add them back after respawn */
    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerPostRespawnEvent.onPlayerRespawn() {
        //TODO add Dead component on death and remove it here
    }
}
