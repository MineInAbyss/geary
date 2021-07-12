package com.mineinabyss.geary.minecraft.systems

import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.minecraft.components.DisplayBossBar
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Handles displaying of boss bars.
 */
public object BossBarDisplaySystem : TickingSystem(interval = 10) {
    override fun QueryResult.tick() {
        // for each entity which has DisplayBossBar component
        DisplayBossBarQuery.apply {
            forEach { result: QueryResult ->
                val displayBossBar: DisplayBossBar = result.entity.get<DisplayBossBar>() ?: return
                val bossBar = Bukkit.createBossBar("Boss Bar", displayBossBar.color, displayBossBar.style)
                for(player in Bukkit.getOnlinePlayers()) {
                    val playerLocation = player.getLocation()
                    val location = result.entity.get<Location>() ?: return
                    if(playerLocation.world.equals(location) && playerLocation.distance(location) <= displayBossBar.range) {
                        bossBar.addPlayer(player)
                    }
                    else {
                        bossBar.removePlayer(player)
                    }
                }
            }

        }
    }
}

public object DisplayBossBarQuery : Query() {
    private val QueryResult.bossbar by get<DisplayBossBar>()
}
