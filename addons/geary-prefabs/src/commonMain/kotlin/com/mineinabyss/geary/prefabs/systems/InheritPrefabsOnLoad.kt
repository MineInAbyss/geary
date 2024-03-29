package com.mineinabyss.geary.prefabs.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.events.PrefabLoaded
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Pointers


class InheritPrefabsOnLoad : Listener() {
    private val Pointers.loaded by family { has<PrefabLoaded>() }.on(event)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        target.entity.inheritPrefabs()
    }
}

