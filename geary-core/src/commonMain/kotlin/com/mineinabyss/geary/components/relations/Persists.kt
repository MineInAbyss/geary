package com.mineinabyss.geary.components.relations

/**
 * When related to another component, ensures it will be persisted if possible.
 *
 * @property hash Used to avoid unnecessary writes when data has not changed.
 */
data class Persists(
    var hash: Int = 0
)
