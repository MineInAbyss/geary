package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.MutableFamily
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

    @OptIn(UnsafeAccessors::class)
    val QueriedEntity.entity get() = unsafeEntity

    internal fun getAccessorsFor(vararg props: KProperty<*>): Array<Accessor> {
        return props.mapNotNull { this@ListenerQuery.props[it.name] }.toTypedArray()
    }
    protected fun EventQueriedEntity.anySet(vararg props: KProperty<*>) = anySet(*getAccessorsFor(*props))

    protected fun EventQueriedEntity.anyRemoved(vararg props: KProperty<*>) = anyRemoved(*getAccessorsFor(*props))

    protected fun EventQueriedEntity.anyAdded(vararg props: KProperty<*>) = anyAdded(*getAccessorsFor(*props))

    protected fun EventQueriedEntity.anyFirstSet(vararg props: KProperty<*>) = anyFirstSet(*getAccessorsFor(*props))

    protected inline fun EventQueriedEntity.forEachAccessorComponent(
        props: Collection<Accessor>,
        crossinline run: MutableFamily.Selector.And.(ComponentId) -> Unit
    ) {
        invoke {
            this@ListenerQuery.accessors.intersect(props.toSet())
                .asSequence()
                .filterIsInstance<FamilyMatching>()
                .mapNotNull { it.family }
                .flatMap { it.components }
                .forEach { run(it) }
        }
    }

    /** Fires when an entity has a component of type [T] set or updated. */
    protected fun EventQueriedEntity.anySet(vararg props: Accessor) {
        forEachAccessorComponent(props.toSet()) { onSet(it) }
    }


    /**
     * Listens to removal of any component read by any of [accessors].
     * Entity will still have the component on it when the event is fire,
     * the component will *always* be removed after all relevant listeners are fired.
     */
    protected fun EventQueriedEntity.anyRemoved(vararg accessors: Accessor) {
        forEachAccessorComponent(accessors.toSet()) { onRemove(it) }
    }


    /** Fires when an entity has a component of type [T] added, updates are not considered since no data changes. */
    protected fun EventQueriedEntity.anyAdded(vararg props: Accessor) {
        forEachAccessorComponent(props.toSet()) { onAdd(it) }
    }

    /** Fires when an entity has a component of type [T] set, only if it was not set before. */
    protected fun EventQueriedEntity.anyFirstSet(vararg props: Accessor) {
        forEachAccessorComponent(props.toSet()) { onFirstSet(it) }
    }

}
