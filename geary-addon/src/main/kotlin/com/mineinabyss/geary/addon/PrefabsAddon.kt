package com.mineinabyss.geary.addon

import com.mineinabyss.geary.addon.GearyLoadPhase.LOAD_PREFABS
import java.nio.file.Path
import kotlin.io.path.name

public class PrefabsAddon(
    private val addon: GearyAddon
) {
    /** Loads prefab entities from all files inside a [directory][from], into a given [namespace] */
    public fun path(
        from: Path,
        namespace: String = from.name
    ): Unit = with(addon) {
        // Start with the innermost directories
        val dirs = from.toFile().walkBottomUp().filter { it.isDirectory }
        val files = dirs.flatMap { dir -> dir.walk().maxDepth(1).filter { it.isFile } }
        files.forEach { file ->
            val entity = prefabManager.loadFromFile(namespace, file) ?: return@forEach
            addonManager.loadingPrefabs += entity
        }
    }
}

public fun GearyAddon.prefabs(init: PrefabsAddon.() -> Unit) {
    startup {
        LOAD_PREFABS {
            PrefabsAddon(this@prefabs).init()
        }
    }
}
