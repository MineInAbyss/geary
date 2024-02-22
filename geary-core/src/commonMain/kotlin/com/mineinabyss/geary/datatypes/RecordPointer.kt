package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes

/** A record created in place that delegates to the real entity pointer the first time [entity] gets accessed. */
class RecordPointer @PublishedApi internal constructor(
    archetype: Archetype,
    row: Int
) {
}
