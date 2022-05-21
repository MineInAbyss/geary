package com.mineinabyss.geary.papermc.listeners

import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope

@AutoScan
class InheritPrefabsOnLoad : GearyListener() {
    private val EventScope.loaded by family { has<PrefabLoaded>() }

    @Handler
    private fun TargetScope.inheritOnLoad() {
        entity.inheritPrefabs()
    }
}

