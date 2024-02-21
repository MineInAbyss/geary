package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.query.QueriedEntity
import kotlin.reflect.KProperty

class RelationsAccessor(
    override val queriedEntity: QueriedEntity,
    val kind: ComponentId,
    val target: EntityId,
) : ReadOnlyAccessor<List<Relation>>, FamilyMatching {
    override val family: Family = family { hasRelation(kind, target) }

    private var cachedRelations = emptyList<Relation>()
    private var cachedArchetype: Archetype? = null

    override fun getValue(thisRef: AccessorOperations, property: KProperty<*>): List<Relation> {
        val archetype = queriedEntity.currArchetype
        if (archetype != cachedArchetype) {
            cachedArchetype = archetype
            cachedRelations = archetype.getRelations(kind, target)
        }

        return cachedRelations
    }
}
