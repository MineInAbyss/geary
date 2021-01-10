package com.mineinabyss.geary.minecraft.engine

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.minecraft.events.EntityRemovedEvent
import com.mineinabyss.geary.minecraft.geary
import com.mineinabyss.idofront.events.call
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey

public class SpigotEngine : GearyEngine() {
    public companion object {
        public val componentsKey: NamespacedKey = NamespacedKey(geary, "components")
    }

    override fun onStart() {
        //tick all systems every interval ticks
        geary.schedule {
            repeating(1)
            while (true) {
                tick(Bukkit.getServer().currentTick.toLong())
                yield()
            }
        }
    }

    override fun removeEntity(entity: GearyEntity) {
        EntityRemovedEvent(entity).call()
        super.removeEntity(entity)
    }
}
