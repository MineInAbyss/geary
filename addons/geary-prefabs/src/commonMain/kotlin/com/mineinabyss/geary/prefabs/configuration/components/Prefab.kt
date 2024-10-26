package com.mineinabyss.geary.prefabs.configuration.components

import kotlinx.io.files.Path

/**
 * A component applied to prefabs loaded from a file that allows them to be reread.
 *
 * @param file The file this prefab was loaded from or null if it is a child in a prefab, or created some other way.
 */
class Prefab(
    val file: Path? = null
)
