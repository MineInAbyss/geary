package com.mineinabyss.geary.components

import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.datatypes.entityTypeOf

/**
 * Entity id references that are used internally [EntityProvider] should ensure to sequentially create entities to
 * account for these. As a result, these ids should be sequential, starting at zero.
 *
 * Keeping these as const vals helps improve performance in tight loops that might be referencing these often.
 */
object ReservedComponents {
    const val COMPONENT_INFO = 0uL
    const val ANY = 1uL
    const val CHILD_OF = 2uL

    val reservedComponents = mapOf(
        ComponentInfo::class to COMPONENT_INFO,
        Any::class to ANY,
        ChildOf::class to CHILD_OF,
    )
}
