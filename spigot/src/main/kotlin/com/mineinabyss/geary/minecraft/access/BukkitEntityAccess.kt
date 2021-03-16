package com.mineinabyss.geary.minecraft.access

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.PrefabKey
import com.mineinabyss.geary.ecs.entities.addPrefab
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.minecraft.events.GearyEntityRemoveEvent
import com.mineinabyss.geary.minecraft.events.GearyMinecraftLoadEvent
import com.mineinabyss.geary.minecraft.hasComponentsEncoded
import com.mineinabyss.geary.minecraft.store.decodeComponentsFrom
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.nms.entity.typeName
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
        entityMap[entity.uniqueId]?.let { return it }

        val gearyEntity: GearyEntity = attachTo ?: Engine.entity {}
        gearyEntity.apply {
            set<Entity>(entity)
            setAll(
                onBukkitEntityRegister.flatMap { mapping ->
                    mutableListOf<GearyComponent>().apply { mapping(entity) }
                }
            )
        }

        //TODO extension function to get prefab key from entity, including correct namespace
        PrefabManager[PrefabKey("Mobzy", entity.typeName)]?.let {
            gearyEntity.addPrefab(it)
        }

        val pdc = entity.persistentDataContainer
        if (pdc.hasComponentsEncoded)
            gearyEntity.decodeComponentsFrom(pdc)

        entityMap[entity.uniqueId] = gearyEntity

        GearyMinecraftLoadEvent(gearyEntity).call()

        return gearyEntity
    }

    private fun unregisterEntity(entity: Entity): GearyEntity? {
        val gearyEntity = entityMap[entity.uniqueId] ?: return null

        for (extension in onBukkitEntityUnregister) {
            extension(gearyEntity, entity)
        }

        return entityMap.remove(entity.uniqueId)
    }

    public fun getEntityOrNull(entity: Entity): GearyEntity? = entityMap[entity.uniqueId]

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
        val gearyEntity = getEntityOrNull(entity) ?: return
        unregisterEntity(entity)
        //FIXME this gets called before saveData so the entity cannot be accessed from there
        // perhaps we could save data here?
        gearyEntity.removeEntity()
    }

    //TODO Is there anything we'd actually want to do with the ECS while the player sees their respawn screen?
    // If so, dont remove them on entity remove event and just refresh the inventory here
    /** Player death counts as an entity being removed from the world so we should add them back after respawn */
    @EventHandler
    public fun PlayerPostRespawnEvent.onPlayerRespawn() {
        registerEntity(player)
    }
}
