package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.dsl.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyDSLMarker
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.prefabs.modules.prefabs
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

class Prefabs(
//    private val addon: GearyAddon
) {
    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    fun Namespaced.path(
        from: Path,
    ) {
        // Start with the innermost directories
        val dirs = from.toFile().walkBottomUp().filter { it.isDirectory }
        val files = dirs.flatMap { dir -> dir.walk().maxDepth(1).filter { it.isFile } }
        files.forEach { file ->
            val entity = prefabs.manager.loadFromFile(namespace, file) ?: return@forEach
//            addonManager.loadingPrefabs += entity
        }
    }

    fun Namespaced.paths(
        folder: Path,
        glob: String = "*",
    ) {
        folder.listDirectoryEntries(glob).forEach(::path)
    }

    companion object : GearyAddon<Prefabs> {
        override fun install(geary: GearyModule) {
            geary.addons
        }
    }
}

@GearyDSLMarker
fun GearyModule.prefabs(configure: Prefabs.() -> Unit) {
    addons.getOrNull<Prefabs>()?.configure() ?: install(Prefabs, configure)
}
