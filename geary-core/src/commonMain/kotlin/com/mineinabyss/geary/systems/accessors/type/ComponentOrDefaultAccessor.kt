package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
class ComponentOrDefaultAccessor<T>(
    val id: ComponentId,
    val default: () -> T,
) : ReadOnlyAccessor<T> {
    private var cachedIndex = -1
    private var cachedArchetype: Archetype? = null

    override fun getValue(thisRef: Query, property: KProperty<*>): T {
        TODO("Not yet implemented")
    }

//    override fun getValue(thisRef: Pointer, property: KProperty<*>): T {
//        val archetype = thisRef.archetype
//        if (archetype !== cachedArchetype) {
//            cachedArchetype = archetype
//            cachedIndex = archetype.indexOf(id)
//        }
//        if (cachedIndex == -1) return default()
//        return archetype.componentData[cachedIndex][thisRef.row] as T
//    }
}
