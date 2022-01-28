package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import org.koin.mp.KoinPlatformTools

//TODO use multiple receivers with Kotlin 1.6.20
public fun GearyType.getArchetype(): Archetype =
    KoinPlatformTools.defaultContext().get().get<Engine>().getArchetype(this)

public fun Archetype.countChildren(vis: IntSet = IntOpenHashSet()): Int {
    componentAddEdges.inner.values.filter { it !in vis }
        .forEach { KoinPlatformTools.defaultContext().get().get<Engine>().getArchetype(it).countChildren(vis) }
    vis.addAll(componentAddEdges.inner.values)
    return vis.count()
}
