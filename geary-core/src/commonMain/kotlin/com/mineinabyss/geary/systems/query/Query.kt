package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.reflect.KProperty

abstract class Query(
    val baseFamily: Family = family {  }
) : AccessorOperations() {

    val target: QueriedEntity = QueriedEntity()

    private val props: MutableMap<KProperty<*>, Family> = mutableMapOf()

    fun buildFamily(): Family = family {
        for (family in props.values) {
            add(family)
        }
    }

    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        family?.let { props[prop] = it }
        return this
    }
}
