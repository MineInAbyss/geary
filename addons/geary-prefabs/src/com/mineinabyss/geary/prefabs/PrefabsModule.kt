package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.observers.queries.cacheAssociatedBy
import com.mineinabyss.geary.systems.query.query
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

data class PrefabsModule(
    override val world: Geary,
    val loader: PrefabLoader,
) : WorldScoped by world.newScope() {
    val prefabs = cacheAssociatedBy(query<PrefabKey>()) { (string) -> string }

    fun create(
        vararg prefabs: Pair<PrefabKey, GearyEntity>,
    ) {
        prefabs.forEach { (prefabKey, entity) ->
            PrefabLoader.markAsPrefab(entity, prefabKey)
        }
    }

    /** Loads prefabs under a [namespace] from a list of paths. */
    fun fromFiles(
        namespace: String,
        vararg from: Path,
    ) {
        loader.load(PrefabPath(namespace, paths = { from.asSequence() }))
    }

    /** Loads prefabs under a [namespace] from a directory by walking it. */
    fun fromDirectory(
        namespace: String,
        folder: Path,
    ) {
        loader.load(PrefabPath(namespace, paths = { walkFolder(folder) }))
    }

    /** Loads prefabs under a [namespace] from a list of manually created [PrefabSource]s. */
    fun fromSources(
        namespace: String,
        vararg sources: PrefabSource,
    ) {
        loader.load(PrefabPath(namespace, sources = { sources.asSequence() }))
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

    /** Gets an entity associated with a [PrefabKey] if it has one. */
    operator fun get(key: PrefabKey): Entity? = prefabs[key]
}