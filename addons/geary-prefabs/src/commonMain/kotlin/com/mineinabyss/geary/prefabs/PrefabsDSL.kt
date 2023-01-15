package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.serialization.dsl.fileSystem
import okio.Path

@GearyDSL
class PrefabsDSL(
    private val namespaced: Namespaced
) {
    private val loader = prefabs.loader

    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    fun from(
        vararg from: Path,
    ) {
        loader.addSource(PrefabPath(namespaced.namespace) { from.asSequence() })
    }

    fun fromRecursive(folder: Path) {
        loader.addSource(PrefabPath(namespaced.namespace) {
            fileSystem
                .listRecursively(folder, true)
                .filter { it.name.endsWith(".yml") }
        })
    }
}
