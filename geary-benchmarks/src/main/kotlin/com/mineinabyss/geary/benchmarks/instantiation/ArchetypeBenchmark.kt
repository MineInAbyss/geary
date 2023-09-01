package com.mineinabyss.geary.benchmarks.instantiation

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class ArchetypeBenchmark {
    var arch: Archetype = Archetype(EntityType(listOf(1uL, 1uL shl 10, 1uL shl 32 or 1uL)), 0)
    val type = EntityType(listOf(1uL, 1uL shl 10, 1uL shl 32 or 1uL))

}
