package com.mineinabyss.geary.minecraft.listeners

import com.mineinabyss.geary.ecs.prefab.inheritPrefabs
import com.mineinabyss.geary.minecraft.events.GearyPrefabLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

public class InheritPrefabsOnLoad : Listener {
    @EventHandler
    private fun GearyPrefabLoadEvent.onPrefabLoad() {
        entity.inheritPrefabs()
    }
}

