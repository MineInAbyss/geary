package com.mineinabyss.geary.minecraft.systems

import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.minecraft.components.BukkitEntityType
import com.mineinabyss.geary.minecraft.components.DisplayBossBar
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * Handles displaying of boss bars.
 */
public object BossBarDisplaySystem : TickingSystem(interval = 10) {
    private val QueryResult.bossbar by get<DisplayBossBar>()
    private val QueryResult.bukkitentity by get<BukkitEntity>()

    override fun QueryResult.tick() {
        forEach { result: QueryResult ->
            val location = bukkitentity.getLocation()
            val playersInRange = location.getNearbyPlayers(bossbar.range)

            for(player in bossbar.playersInRange subtract playersInRange) {
                bossbar.bossBar.removePlayer(player)
            }

            bossbar.playersInRange = playersInRange

            for(player in playersInRange) {
                bossbar.bossBar.addPlayer(player)
            }
        }
    }
}