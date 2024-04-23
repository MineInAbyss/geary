package com.mineinabyss.geary.events.queries

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.events.types.OnAdd
import com.mineinabyss.geary.events.types.OnRemove
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.Query

fun <T, Q : Query> GearyModule.cacheAsMap(query: Q, associateBy: (Q) -> T): ObserveQueryAssociatedBy<T, Q> {
    return object : ObserveQueryAssociatedBy<T, Q>(query, this) {
        override fun associateBy(query: Q): T = associateBy(query)
    }
}

abstract class ObserveQueryAssociatedBy<T, Q : Query>(private val query: Q, geary: GearyModule) {
    val map = mutableMapOf<T, Entity>()
    abstract fun associateBy(query: Q): T

    operator fun get(key: T): Entity? = map[key]
    operator fun set(key: T, value: Entity) {
        map[key] = value
    }

    fun <R> query(key: T, ifExists: (Q) -> R): R? {
        return map[key]?.let { ifExists(query) }
    }

    init {
        geary.observe<OnAdd>().filter(query).exec {
            map[associateBy(it)] = entity
        }
        geary.observe<OnRemove>().filter(query).exec {
            map.remove(associateBy(it))
        }
    }
}
