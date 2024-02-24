package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.components.events.ExtendedEntity
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import kotlin.reflect.KProperty

abstract class ListenerQuery : QueriedEntity() {
    val event: EventQueriedEntity = EventQueriedEntity()
    val source: QueriedEntity = QueriedEntity()

    data class Families(
        val event: Family.Selector.And,
        val target: Family.Selector.And,
        val source: Family.Selector.And,
    )

    fun buildFamilies(): Families = Families(
        event = event.buildFamily(),
        target = this.buildFamily(),
        source = source.buildFamily(),
    )

    protected open fun ensure() {}

    internal fun initialize() = ensure()

    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        family?.let { queriedEntity.props[prop] = it }
        return this
    }

    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    protected fun onAdd(vararg props: KProperty<*>) {

    }

    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
    protected fun onFirstSet(vararg props: KProperty<*>) {

    }
}
