package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Namespaced
import okio.Path

class PrefabsConfiguration {
    val loader = prefabs.loader

    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    fun Namespaced.files(
        vararg from: Path,
    ) {
        loader.addSource(PrefabPath(namespace) { from.asSequence() })
    }

    fun Namespaced.glob(
        folder: Path,
        glob: String = "**",
    ) {
        loader.addSource(PrefabPath(namespace) {
            Files.newDirectoryStream(folder, glob)
                .filter { it.isRegularFile() }
                .asSequence()
        })
    }
}
