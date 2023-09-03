package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.AccessorThisRef
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
open class RemovableComponentAccessor<T>(
    val id: ComponentId,
) : ReadWriteAccessor<T?>, FamilyMatching {
    override val family: Family = family { hasSet(id) }

    private var cachedIndex = -1
    private var cachedArchetype: Archetype? = null

    override fun getValue(thisRef: AccessorThisRef, property: KProperty<*>): T? {
        val archetype = thisRef.archetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
        }

        if (cachedIndex == -1) return null
        return archetype.componentData[cachedIndex][thisRef.row] as T
    }

    override fun setValue(thisRef: AccessorThisRef, property: KProperty<*>, value: T?) {
        val archetype = thisRef.archetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
        }

        if (cachedIndex == -1) {
            if (value == null) return
            else thisRef.entity.set(value, id)
            return
        }

        if (value == null) thisRef.entity.remove(id)
        else archetype.componentData[cachedIndex][thisRef.row] = value
    }
}
