package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.soywiz.kds.IntSet
import com.soywiz.kds.intSetOf

public fun EntityType.getArchetype(): Archetype =
    globalContext.engine.getArchetype(this)

public fun Archetype.countChildren(vis: IntSet = intSetOf()): Int {
    componentAddEdges.inner.values.filter { it !in vis }
        .forEach { globalContext.engine.getArchetype(it).countChildren(vis) }
    vis.addAll(componentAddEdges.inner.values)
    return vis.count()
}
