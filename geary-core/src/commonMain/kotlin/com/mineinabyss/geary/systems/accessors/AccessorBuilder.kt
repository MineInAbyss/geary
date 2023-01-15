package com.mineinabyss.geary.systems.accessors

/**
 * A builder that can provide an accessor for [AccessorHolder]s.
 *
 * @see Accessor
 */
fun interface AccessorBuilder<out T : Accessor<*>> {
    //TODO we shouldn't expose index here since not all Accessors have one
    /** Provides an [Accessor] to a [holder] and [index] this accessor should be placed in for that holder. */
    fun build(holder: AccessorHolder, index: Int): T
}
