package com.mineinabyss.geary.minecraft.access

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.entity
import com.mineinabyss.geary.minecraft.events.GearyEntityRemoveEvent
import com.mineinabyss.geary.minecraft.hasComponentsEncoded
import com.mineinabyss.geary.minecraft.store.decodeComponentsFrom
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import java.util.*
import kotlin.collections.set

internal typealias OnEntityRegister = MutableList<GearyComponent>.(Entity) -> Unit
internal typealias OnEntityUnregister = (GearyEntity, Entity) -> Unit

public object BukkitEntityAccess : Listener {
    private val entityMap = mutableMapOf<UUID, GearyEntity>()
    internal val bukkitEntityAccessExtensions = mutableSetOf<Entity.() -> GearyEntity?>()
    internal val onBukkitEntityRegister: MutableList<OnEntityRegister> = mutableListOf()
    internal val onBukkitEntityUnregister: MutableList<OnEntityUnregister> = mutableListOf()

    public fun onBukkitEntityRegister(add: OnEntityRegister) {
        onBukkitEntityRegister.add(add)
    }

    public fun onBukkitEntityUnregister(add: OnEntityUnregister) {
        onBukkitEntityUnregister.add(add)
    }


    public fun <T : Entity> registerEntity(entity: T, gearyEntity: GearyEntity? = null): GearyEntity {
        //if the entity is already registered, return it
        entityMap[entity.uniqueId]?.let { return it }

        val createdEntity: GearyEntity = gearyEntity ?: Engine.entity {
            addComponent<Entity>(entity)
            addComponents(
                onBukkitEntityRegister.flatMap { mapping ->
                    mutableListOf<GearyComponent>().apply { mapping(entity) }
                }
            )
        }

        val pdc = entity.persistentDataContainer
        if (pdc.hasComponentsEncoded)
            createdEntity.decodeComponentsFrom(pdc)

        entityMap[entity.uniqueId] = createdEntity

        return createdEntity
    }

    private fun unregisterEntity(entity: Entity): GearyEntity? {
        val gearyEntity = entityMap[entity.uniqueId] ?: return null

        for (extension in onBukkitEntityUnregister) {
            extension(gearyEntity, entity)
        }

        return entityMap.remove(entity.uniqueId)
    }

    public fun getEntityOrNull(entity: Entity): GearyEntity? =
        entity as? GearyEntity
        //TODO entity.toNMS() as? GearyEntity ?:
        //try to find the mob in the map
            ?: entityMap[entity.uniqueId]
            //try to get first non null mapping in registered extensions
            ?: bukkitEntityAccessExtensions
                .asSequence()
                .map { it(entity) }
                .firstOrNull { it != null }

    public fun <T : Entity> getEntity(entity: T): GearyEntity =
        getEntityOrNull(entity) ?: registerEntity(entity)

    @EventHandler
    public fun GearyEntityRemoveEvent.onEntityRemoved() {
        entity.toBukkit<Entity>()?.let {
            unregisterEntity(it)
        }
    }

    @EventHandler
    public fun PlayerLoginEvent.onPlayerLogin() {
        registerEntity(player)
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler
    public fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        val gearyEntity = entityMap[entity.uniqueId] ?: return
        unregisterEntity(entity)
        Engine.removeEntity(gearyEntity)
    }

    //TODO Is there anything we'd actually want to do with the ECS while the player sees their respawn screen?
    // If so, dont remove them on entity remove event and just refresh the inventory here
    /** Player death counts as an entity being removed from the world so we should add them back after respawn */
    @EventHandler
    public fun PlayerPostRespawnEvent.onPlayerRespawn() {
        registerEntity(player)
    }
}
