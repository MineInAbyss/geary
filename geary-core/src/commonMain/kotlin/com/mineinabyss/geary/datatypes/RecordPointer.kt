package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes

/** A record created in place that delegates to the real entity pointer the first time [entity] gets accessed. */
class RecordPointer @PublishedApi internal constructor(
    archetype: Archetype,
    row: Int
) {
    constructor(record: Record) : this(record.archetype, record.row) {
        delegated = true
        delegate = record
    }

    private val originalArchetype = archetype
    private val originalRow = row

    @UnsafeAccessors
    val archetype: Archetype get() = if (delegated) delegate!!.archetype else originalArchetype
    val row: Int get() = if (delegated) delegate!!.row else originalRow

    private var delegate: Record? = null
    private var delegated = false

    @UnsafeAccessors
    val entity: Entity
        get() {
            val entity = archetype.getEntity(row)
            if (!delegated) {
                delegate = archetypes.records[entity]
            }
            delegated = true
            return entity
        }

    @UnsafeAccessors
    operator fun component1(): Archetype = archetype

    operator fun component2(): Int = row
}
