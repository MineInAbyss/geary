package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.components.events.ExtendedEntity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor
import kotlin.reflect.KProperty

abstract class EventQuery : AccessorOperations() {
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

//    /** Fires when an entity has a component of type [T] set or updated. */
//    inline fun <reified T : ComponentAccessor<A>, reified A> T.whenSetOnTarget(): ReadWriteEntitySelectingAccessor<T, A> {
//        event.mutableFamily.onSet(componentId<A>())
//        return this.on(target)
//    }

    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
    inline fun <reified T : ComponentAccessor<A>, reified A> T.whenFirstSetOnTarget(): ReadWriteEntitySelectingAccessor<T, A> {
        event.mutableFamily.onFirstSet(componentId<A>())
        return this.on(target)
    }

    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    inline fun <reified T : ComponentAccessor<A>, reified A> T.whenAddedOnTarget(): ReadWriteEntitySelectingAccessor<T, A> {
        event.mutableFamily.onAdd(componentId<A>())
        return this.on(event)
    }

    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    fun whenExtendedEntity(): ReadOnlyEntitySelectingAccessor<ReadOnlyAccessor<GearyEntity>, GearyEntity> {
        event.mutableFamily.onExtendedEntity()
        return getRelations<ExtendedEntity?, Any?>().map { it.single().target.toGeary() }.on(event)
    }

    fun onAnySet(vararg props: KProperty<*>) {
        event.family {
            props.mapNotNull { event.props[it] }.forEach {
                if (it is Family.Leaf.Component)
                    onSet(it.component)
                // TODO do we error here if not, this isn't really typesafe?
            }
        }
    }

}
