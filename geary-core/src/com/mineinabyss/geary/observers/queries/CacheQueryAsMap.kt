package com.mineinabyss.geary.observers.queries

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.builders.ObserverContext
import com.mineinabyss.geary.observers.events.OnFirstSet
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.systems.query.ShorthandQuery

fun <T, Q : ShorthandQuery> WorldScoped.cacheGroupedBy(
    query: Q,
    groupBy: ObserverContext.(Q) -> T,
): QueryGroupedBy<T, Q> = addCloseable(object : QueryGroupedBy<T, Q>(query, world) {
    override fun ObserverContext.groupBy(query: Q): T = groupBy(query)
})

fun <T, Q : ShorthandQuery> WorldScoped.cacheAssociatedBy(
    query: Q,
    associateBy: ObserverContext.(Q) -> T,
): QueryAssociatedBy<T, Q> = addCloseable(object : QueryAssociatedBy<T, Q>(query, this.world) {
    override fun ObserverContext.associateBy(query: Q): T = associateBy(query)
})

abstract class QueryGroupedBy<T, Q : ShorthandQuery>(private val query: Q, geary: Geary) :
    WorldScoped by geary.newScope() {
    private val map = mutableMapOf<T, MutableList<Entity>>()

    abstract fun ObserverContext.groupBy(query: Q): T

    operator fun get(key: T): List<Entity> = map[key] ?: listOf()

    private fun add(key: T, value: Entity) {
        map.getOrPut(key) { mutableListOf() }.add(value)
    }

    private fun remove(key: T, value: Entity) {
        map[key]?.remove(value)
    }

    init {
        observe<OnFirstSet>().involving(query).exec {
            add(groupBy(it), entity)
        }
        observe<OnRemove>().involving(query).exec {
            remove(groupBy(it), entity)
        }
    }
}

abstract class QueryAssociatedBy<T, Q : ShorthandQuery>(private val query: Q, geary: Geary) : AutoCloseable {
    private val map = mutableMapOf<T, Entity>()

    abstract fun ObserverContext.associateBy(query: Q): T

    operator fun get(key: T): Entity? = map[key]

    val keys get() = map.keys.toList()
    val values get() = map.values.toList()
    val entries get() = map.entries.toList()

    fun <R> query(key: T, ifExists: (Q) -> R): R? {
        return map[key]?.let { ifExists(query) }
    }

    private val onSetObserver = geary.observe<OnFirstSet>().involving(query).exec {
        map[associateBy(it)] = entity
    }

    private val onRemoveObserver = geary.observe<OnRemove>().involving(query).exec {
        map.remove(associateBy(it))
    }

    override fun close() {
        onSetObserver.close()
        onRemoveObserver.close()
        map.clear()
    }
}
