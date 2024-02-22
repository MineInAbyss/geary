package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.reflect.KProperty

abstract class ListenerQuery : AccessorOperations() {
    val target: QueriedEntity = QueriedEntity()
    val event: QueriedEntity = QueriedEntity()
    val source: QueriedEntity = QueriedEntity()

    data class Families(
        val event: Family.Selector.And,
        val target: Family.Selector.And,
        val source: Family.Selector.And,
    )

    fun buildFamilies(): Families = Families(
        event = event.buildFamily(),
        target = target.buildFamily(),
        source = source.buildFamily(),
    )

    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : FamilyMatching> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        family?.let { queriedEntity.props[prop] = it }
        return this
    }

    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    fun onExtend() {
        event.match { onExtendedEntity() }
//        return getRelations<ExtendedEntity?, Any?>().map { it.single().target.toGeary() }.on(event)
    }

    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    fun onAdd(vararg props: KProperty<*>) {

    }

    /** Fires when an entity has a component of type [T] set or updated. */
    fun onSet(vararg props: KProperty<*>) {
        val names = props.map { it.name }.toSet()
        event.match {
            target.props.filterKeys { prop ->
                prop.name in names
            }.values.flatMap {
                it.components
            }.forEach { component ->
                onSet(component)
                // TODO do we error here if not, this isn't really typesafe?
            }
        }
    }

    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
    fun onFirstSet(vararg props: KProperty<*>) {

    }
}
