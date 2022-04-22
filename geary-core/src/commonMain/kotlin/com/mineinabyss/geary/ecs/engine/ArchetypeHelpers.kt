package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.context.globalContext
import com.soywiz.kds.IntSet
import com.soywiz.kds.intSetOf
import org.koin.mp.KoinPlatformTools

//TODO use multiple receivers with Kotlin 1.6.20
public fun GearyType.getArchetype(): Archetype =
    globalContext.engine.getArchetype(this)

public fun Archetype.countChildren(vis: IntSet = intSetOf()): Int {
    componentAddEdges.inner.values.filter { it !in vis }
        .forEach { globalContext.engine.getArchetype(it).countChildren(vis) }
    vis.addAll(componentAddEdges.inner.values)
    return vis.count()
}
