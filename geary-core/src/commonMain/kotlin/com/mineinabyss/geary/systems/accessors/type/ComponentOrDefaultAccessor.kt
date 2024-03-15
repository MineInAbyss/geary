package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.query.QueriedEntity
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
class ComponentOrDefaultAccessor<T>(
    override val originalAccessor: Accessor?,
    override val queriedEntity: QueriedEntity,
    val id: ComponentId,
    val default: () -> T,
) : ReadOnlyAccessor<T> {
    private var cachedIndex = -1
    private var cachedArchetype: Archetype? = null

    override fun getValue(thisRef: Query, property: KProperty<*>): T {
        val archetype = queriedEntity.archetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
        }
        if (cachedIndex == -1) return default()
        return archetype.componentData[cachedIndex][queriedEntity.row] as T
    }
}
