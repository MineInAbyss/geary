package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.*
import com.mineinabyss.geary.systems.query.QueriedEntity
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
class RelationsWithDataAccessor<K, T>(
    override val originalAccessor: Accessor?,
    override val queriedEntity: QueriedEntity,
    val kind: ComponentId,
    val target: EntityId,
) : ReadOnlyAccessor<List<RelationWithData<K, T>>>, FamilyMatching {
    override val family = family { hasRelation(kind, target) }

    private var cachedRelations = emptyList<Relation>()
    private var cachedArchetype: Archetype? = null

    override fun getValue(thisRef: Query, property: KProperty<*>): List<RelationWithData<K, T>> {
        val archetype = queriedEntity.archetype
        if (archetype != cachedArchetype) {
            cachedArchetype = archetype
            cachedRelations = archetype.getRelations(kind, target)
        }

        @Suppress("UNCHECKED_CAST")
        return archetype.readRelationDataFor(
            queriedEntity.row,
            kind,
            target,
            cachedRelations
        ) as List<RelationWithData<K, T>>
    }
}
