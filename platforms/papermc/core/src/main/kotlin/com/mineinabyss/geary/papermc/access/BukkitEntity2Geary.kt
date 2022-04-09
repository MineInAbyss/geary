package com.mineinabyss.geary.papermc.access

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.FormatsContext
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.engine.systems
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.QueryContext
import com.mineinabyss.geary.ecs.events.EntityRemoved
import com.mineinabyss.geary.papermc.BukkitEntityAssociationsContext
import com.mineinabyss.geary.papermc.PaperEngineContext
import com.mineinabyss.geary.papermc.PluginContext
import com.mineinabyss.geary.papermc.store.decodeComponentsFrom
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.geary.papermc.store.hasComponentsEncoded
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.typealiases.BukkitEntity
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.collections.set

context(QueryContext, PaperEngineContext, PluginContext, FormatsContext)
public class BukkitEntity2Geary : Listener, BukkitEntityAssociationsContext {
    override val bukkit2Geary: BukkitEntity2Geary = this
    private val entityMap = Int2LongOpenHashMap().apply { defaultReturnValue(-1) }

    public fun startTracking() {
        geary.registerEvents(this)
        systems(Register(), Unregister())
    }

    public operator fun get(entityId: Int): GearyEntity? {
        val id = entityMap.get(entityId)
        if (id == -1L) return null
        return id.toGeary()
    }

    public operator fun set(bukkit: BukkitEntity, entity: GearyEntity) {
        entityMap[bukkit.entityId] = entity.id.toLong()
    }

    public operator fun contains(entityId: Int): Boolean = entityMap.containsKey(entityId)

    public fun remove(entityId: Int): GearyEntity? {
        return entityMap.remove(entityId).takeIf { it != -1L }?.toGeary()
    }

    private inner class Register : GearyListener() {
        val TargetScope.bukkit by added<BukkitEntity>()

        @Handler
        fun TargetScope.loadPersisted() {
            set(bukkit, entity)

            val pdc = bukkit.persistentDataContainer
            if (pdc.hasComponentsEncoded)
                entity.decodeComponentsFrom(pdc)
            // allow us to both get the BukkitEntity and specific class (ex Player)
            bukkit.type.entityClass?.kotlin?.let { bukkitClass ->
                entity.set(bukkit, bukkitClass)
            }

            entity.set(bukkit.uniqueId)
        }
    }

    private inner class Unregister : GearyListener() {
        val TargetScope.bukkit by get<BukkitEntity>()

        override fun onStart() {
            event.has<EntityRemoved>()
        }

        @Handler
        fun TargetScope.persistComponents() {
            remove(bukkit.entityId)
            entity.encodeComponentsTo(bukkit)
        }
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.LOWEST)
    public fun EntityAddToWorldEvent.onBukkitEntityAdd() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        entity.toGeary()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerJoinEvent.onPlayerLogin() {
        player.toGeary()
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST)
    public fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        entity.toGearyOrNull()?.removeEntity()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerQuitEvent.onPlayerLogout() {
        player.toGearyOrNull()?.removeEntity()
    }

    /** Player death counts as an entity being removed from the world so we should add them back after respawn */
    @EventHandler(priority = EventPriority.LOWEST)
    public fun PlayerPostRespawnEvent.onPlayerRespawn() {
        //TODO add Dead component on death and remove it here
        // Or should we add an inactive component that prevents systems from iterating over dead players?
    }
}
