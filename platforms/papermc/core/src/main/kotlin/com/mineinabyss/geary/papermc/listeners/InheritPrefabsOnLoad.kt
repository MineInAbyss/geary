package com.mineinabyss.geary.papermc.listeners

import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs

@AutoScan
public class InheritPrefabsOnLoad : GearyListener() {
    init {
        event.has<PrefabLoaded>()
    }

    @Handler
    private fun TargetScope.inheritOnLoad() {
        entity.inheritPrefabs()
    }
}

