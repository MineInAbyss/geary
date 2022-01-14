package com.mineinabyss.geary.minecraft.engine

import co.aikar.timings.Timings
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.minecraft.GearyPlugin
import com.mineinabyss.geary.minecraft.events.GearyEntityRemoveEvent
import com.mineinabyss.geary.minecraft.gearyPlugin
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.plugin.registerEvents
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import java.util.*

public class SpigotEngine : GearyEngine() {
    public companion object {
        public val componentsKey: NamespacedKey = NamespacedKey(GearyPlugin.instance, "components")
    }

    override fun TickingSystem.runSystem() {
        // Adds a line in timings report showing which systems take up more time.
        val timing = Timings.ofStart(GearyPlugin.instance, javaClass.name)
        runCatching {
            doTick()
        }.apply {
            // We want to stop the timing no matter what, but still propagate error up
            timing.stopTiming()
        }.getOrThrow()
    }

    override fun addSystem(system: GearySystem) {
        super.addSystem(system)

        if (system is Listener)
            gearyPlugin.registerEvents(system)
    }

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks
        GearyPlugin.instance.schedule {
            repeating(1)
            while (true) {
                tick(Bukkit.getServer().currentTick.toLong())
                yield()
            }
        }
    }

    override fun removeEntity(entity: GearyEntity) {
        if (entity.has<UUID>())
        //TODO this should be unnecessary now
            GearyEntityRemoveEvent(entity).call()
        super.removeEntity(entity)
    }
}
