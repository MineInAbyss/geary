package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Entity

/**
 * Allow addons to provide extra information about an entity.
 *
 * Used when calling [Entity.toString]
 */
class EntityInfoReader {
    private val lines = mutableMapOf<String, (Entity) -> String?>()

    fun addInfoLine(name: String, eval: (Entity) -> String?) {
        lines[name] = eval
    }

    init {
        addInfoLine("id") { it.id.toString() }
    }

    fun readEntityInfo(entity: Entity): String {
        return lines.mapNotNull {
            val value = it.value(entity) ?: return@mapNotNull null
            "${it.key}=$value"
        }.joinToString(", ")
    }
}
