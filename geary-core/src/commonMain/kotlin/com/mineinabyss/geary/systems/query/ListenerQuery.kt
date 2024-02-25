package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.reflect.KProperty

abstract class ListenerQuery : Query() {
    override val cacheAccessors: Boolean = false
    val event: EventQueriedEntity = EventQueriedEntity()
    val source: QueriedEntity = QueriedEntity(false)

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

    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    protected fun onAdd(vararg props: KProperty<*>) {

    }

    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
    protected fun onFirstSet(vararg props: KProperty<*>) {

    }

    @OptIn(UnsafeAccessors::class)
    val QueriedEntity.entity get() = unsafeEntity

    protected fun EventQueriedEntity.anySet(vararg props: KProperty<*>) {
        anySet(*props.mapNotNull { this@ListenerQuery.props[it.name] }.toTypedArray())
    }

    /** Fires when an entity has a component of type [T] set or updated. */
    protected fun EventQueriedEntity.anySet(vararg props: Accessor) {
        invoke {
            this@ListenerQuery.accessors.intersect(props.toSet())
                .asSequence()
                .filterIsInstance<FamilyMatching>()
                .mapNotNull { it.family }
                .flatMap { it.components }
                .forEach { component ->
                    onSet(component)
                    // TODO do we error here if not, this isn't really typesafe?
                }
        }
    }
}
