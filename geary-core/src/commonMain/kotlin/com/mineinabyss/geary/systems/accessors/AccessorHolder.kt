package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.MutableFamily


/**
 * A holder of [IndexedAccessor]s that provides logic for reading data off them and calculating their per-archetype cache.
 *
 * @property family A lazily built immutable family that represents all data this holder needs to function.
 */
open class AccessorHolder : AccessorOperations() {
    val family: Family.Selector.And get() = mutableFamily

    @PublishedApi
    internal val mutableFamily: MutableFamily.Selector.And = MutableFamily.Selector.And()


    /** Is the family of this holder not restricted in any way? */
    val isEmpty: Boolean get() = family.and.isEmpty()
}
