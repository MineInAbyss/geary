package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.has
import com.mineinabyss.geary.ecs.components.with
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.entity
import com.mineinabyss.geary.ecs.remove
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import com.mineinabyss.geary.minecraft.events.EntityRemovedEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.collections.set

public object BukkitEntityAccess : Listener {
    private val mobMap = mutableMapOf<Entity, GearyEntity>()
    internal val bukkitEntityAccessExtensions = mutableSetOf<Entity.() -> GearyEntity?>()
    internal val playerRegistryExtensions = mutableListOf<MutableList<GearyComponent>.(Player) -> Unit>()
    internal val playerUnregisterExtensions = mutableListOf<(GearyEntity, Player) -> Unit>()

    private fun registerEntity(entity: Entity, gearyEntity: GearyEntity) {
        mobMap[entity] = gearyEntity
    }

    private fun removeEntity(entity: Entity) = mobMap.remove(entity)

    public fun getEntity(entity: Entity): GearyEntity? =
            //get first non null mapping in registered extensions
            bukkitEntityAccessExtensions
                    .asSequence()
                    .map { it(entity) }
                    .firstOrNull { it != null }
            //or try to find the mob in the map
                    ?: mobMap[entity]
    //TODO a way of getting ID given a vanilla entity as fallback

    public fun getBukkitEntity(entity: GearyEntity): Entity? =
        mobMap.entries.find { entity === it.value }?.key

    public fun registerPlayer(player: Player) {
        registerEntity(player,
                Engine.entity {
                    addComponents(setOf(PlayerComponent(player.uniqueId)) +
                            playerRegistryExtensions.flatMap { mapping ->
                                mutableListOf<GearyComponent>().apply { mapping(player) }
                            })
                }
        )
    }

    public fun unregisterPlayer(player: Player) {
        val gearyPlayer = geary(player) ?: return

        for (extension in playerUnregisterExtensions) {
            extension(gearyPlayer, player)
        }

        gearyPlayer.remove()
        removeEntity(player)
    }

    @EventHandler
    public fun onEntityRemoved(e: EntityRemovedEvent) {
        //clear itself from parent and children
        e.entity.apply {
            with<PlayerComponent> { (player) ->
                removeEntity(player)
            } //TODO might be better to move elsewhere, also handle for all bukkit entities
        }
    }
}

public fun bukkit(entity: GearyEntity): Entity? = BukkitEntityAccess.getBukkitEntity(entity)

//TODO allow mobzy to add onto this
public fun geary(entity: Entity): GearyEntity? = BukkitEntityAccess.getEntity(entity)

public inline fun geary(entity: Entity, run: GearyEntity.() -> Unit): GearyEntity? = geary(entity)?.apply(run)

//TODO add the rest of the GearyEntity operations here
public inline fun <reified T : GearyComponent> Entity.get(): T? = geary(this)?.get()

public inline fun <reified T : GearyComponent> Entity.with(let: (T) -> Unit): Unit? = geary(this)?.get<T>()?.let(let)

public inline fun <reified T : GearyComponent> Entity.has(): Boolean = geary(this)?.has<T>() ?: false
