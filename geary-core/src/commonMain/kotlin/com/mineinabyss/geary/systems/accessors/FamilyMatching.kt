package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.query.QueriedEntity

/**
 * Used for accessors that require a family to be matched against to work
 * (ex a component accessor needs the component present on the entity.)
 */
interface FamilyMatching {
    val queriedEntity: QueriedEntity
    val family: Family.Selector?
}
