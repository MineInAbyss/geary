package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.has
import com.mineinabyss.geary.ecs.components.with
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.entity
import com.mineinabyss.geary.ecs.events.EntityRemovedEvent
import com.mineinabyss.geary.ecs.remove
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.collections.set

public object BukkitEntityAccess : Listener {
    private val mobMap = mutableMapOf<Entity, GearyEntity>()

    private fun registerEntity(entity: Entity, gearyEntity: GearyEntity) {
        mobMap[entity] = gearyEntity
    }

    private fun removeEntity(entity: Entity) = mobMap.remove(entity)

    public fun getEntity(entity: Entity): GearyEntity? = mobMap[entity]
    //TODO a way of getting ID given a vanilla entity as fallback


    public fun registerPlayer(player: Player) {
        registerEntity(player,
                Engine.entity {
                    //TODO allow Looty to add onto this
                    addComponents(setOf(PlayerComponent(player.uniqueId)/*, ChildItemCache()*/))
                }
        )
    }

    public fun unregisterPlayer(player: Player) {
        val gearyPlayer = geary(player) ?: return
        //TODO allow Looty to add onto this
        /*ItemTrackerSystem.apply {
            val inventory = player.get<ChildItemCache>() ?: return
            inventory.updateAndSaveItems(player.inventory, gearyPlayer)
            inventory.clear()
        }*/
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

//TODO allow mobzy to add onto this
public fun geary(entity: Entity): GearyEntity? = /*entity.toMobzy() ?: */BukkitEntityAccess.getEntity(entity)
public inline fun geary(entity: Entity, run: GearyEntity.() -> Unit): GearyEntity? = (/*entity.toMobzy() ?: */BukkitEntityAccess.getEntity(entity))?.apply(run)

//TODO add the rest of the GearyEntity operations here
public inline fun <reified T : GearyComponent> Entity.get(): T? = geary(this)?.get()

public inline fun <reified T : GearyComponent> Entity.with(let: (T) -> Unit): Unit? = geary(this)?.get<T>()?.let(let)

public inline fun <reified T : GearyComponent> Entity.has(): Boolean = geary(this)?.has<T>() ?: false
