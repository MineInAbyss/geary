package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.dsl.GearyInstall
import com.mineinabyss.geary.prefabs.modules.prefabs
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class Prefabs(
//    private val addon: GearyAddon
) {
    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    fun path(
        from: Path,
        namespace: String = from.name
    ) {
        // Start with the innermost directories
        val dirs = from.toFile().walkBottomUp().filter { it.isDirectory }
        val files = dirs.flatMap { dir -> dir.walk().maxDepth(1).filter { it.isFile } }
        files.forEach { file ->
            val entity = prefabs.manager.loadFromFile(namespace, file) ?: return@forEach
            addonManager.loadingPrefabs += entity
        }
    }

    fun paths(
        folder: Path,
        glob: String = "*",
        namespace: String = folder.name
    ) {
        folder.listDirectoryEntries(glob).forEach(::path)
    }

    companion object: GearyInstall<Prefabs> {
        fun install() {

        }
    }
}
