package com.mineinabyss.geary.components.relations

/**
 * When related to another component, ensures it will be persisted if possible.
 *
 * @property hash Used to avoid unnecessary writes when data has not changed.
 */
public data class Persists(
    public var hash: Int = 0
)
