package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope

class InheritPrefabsOnLoad : Listener() {
    private val EventScope.loaded by family { has<PrefabLoaded>() }

    @Handler
    private fun TargetScope.inheritOnLoad() {
        entity.inheritPrefabs()
    }
}

