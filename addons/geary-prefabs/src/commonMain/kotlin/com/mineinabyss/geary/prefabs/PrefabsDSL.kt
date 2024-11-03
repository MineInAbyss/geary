package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.datatypes.GearyEntity
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

@GearyDSL
class PrefabsDSL(
    internal val prefabsBuilder: PrefabSources,
    internal val namespaced: Namespaced,
) {
    fun create(vararg prefabs: Pair<String, GearyEntity>) {
        prefabs.forEach { (name, entity) ->
            PrefabLoader.markAsPrefab(entity, PrefabKey.of(namespaced.namespace, name))
        }
    }

    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    fun fromFiles(
        vararg from: Path,
    ) {
        prefabsBuilder.paths.add(
            PrefabPath(namespaced.namespace, paths = { from.asSequence() })
        )
    }

    fun fromDirectory(folder: Path) {
        prefabsBuilder.paths.add(
            PrefabPath(namespaced.namespace, paths = { walkFolder(folder) })
        )
    }

    fun fromSources(vararg sources: PrefabSource) {
        prefabsBuilder.paths.add(
            PrefabPath(namespaced.namespace, sources = { sources.asSequence() })
        )
    }

    private fun walkFolder(folder: Path): Sequence<Path> = sequence {
        val fileSystem = SystemFileSystem
        val stack = ArrayDeque<Path>()
        stack.add(folder)

        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            if (fileSystem.metadataOrNull(current)?.isDirectory == true) {
                fileSystem.list(current).forEach { stack.add(it) }
            } else {
                yield(current)
            }
        }
    }
}
