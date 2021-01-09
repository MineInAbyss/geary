package com.mineinabyss.geary.minecraft.engine

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.minecraft.events.EntityRemovedEvent
import com.mineinabyss.geary.minecraft.geary
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.logError
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey

public class SpigotEngine : GearyEngine() {
    public companion object {
        public val componentsKey: NamespacedKey = NamespacedKey(geary, "components")
    }

    init {
        //TODO refactor
        //tick all systems every interval ticks
        geary.schedule {
            repeating(1)
            //TODO support suspending functions for systems
            // perhaps async support in the future
            while (true) {
                val currTick = Bukkit.getServer().currentTick
                registeredSystems
                    .filter { currTick % it.interval == 0 }
                    .forEach {
                        try {
                            it.tick()
                        } catch (e: Exception) {
                            logError("Error while running system ${it.javaClass.name}")
                            e.printStackTrace()
                        }
                    }
                yield()
            }
        }
    }

    override fun removeEntity(entity: GearyEntity) {
        EntityRemovedEvent(entity).call()
        super.removeEntity(entity)
    }
}
