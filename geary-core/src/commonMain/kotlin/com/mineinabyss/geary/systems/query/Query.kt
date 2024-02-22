package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.reflect.KProperty

abstract class Query: QueriedEntity() {

    //TODO duplicate with EventQuery
    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        family?.let { queriedEntity.props[prop] = it }
        return this
    }
}
