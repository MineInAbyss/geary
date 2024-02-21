package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes

class QueriedEntity {
    @PublishedApi
    internal var currArchetype: Archetype = archetypes.archetypeProvider.rootArchetype
    @PublishedApi
    internal var currEntityIndex: Int = 0

    @UnsafeAccessors
    val entity: GearyEntity = TODO()
}
