package com.mineinabyss.geary.minecraft.engine

import co.aikar.timings.Timings
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.minecraft.events.GearyEntityRemoveEvent
import com.mineinabyss.geary.minecraft.geary
import com.mineinabyss.idofront.events.call
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import java.util.*

public class SpigotEngine : GearyEngine() {
    public companion object {
        public val componentsKey: NamespacedKey = NamespacedKey(geary, "components")
    }

    override fun TickingSystem.runSystem() {
        // Adds a line in timings report showing which systems take up more time.
        val timing = Timings.ofStart(geary, javaClass.name)
        runCatching {
            doTick()
        }.apply {
            // We want to stop the timing no matter what, but still propagate error up
            timing.stopTiming()
        }.getOrThrow()
    }

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks
        geary.schedule {
            repeating(1)
            while (true) {
                tick(Bukkit.getServer().currentTick.toLong())
                yield()
            }
        }
    }

    override fun removeEntity(entity: GearyEntityId) {
        if (geary(entity).has<UUID>())
            GearyEntityRemoveEvent(geary(entity)).call()
        super.removeEntity(entity)
    }
}
