package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.globalEngine

@JvmInline
public value class Record(public val id: Long) {
    //Upper 32 bits are row id
    public val archetype: Archetype get() = globalEngine.getArchetype((id shr 32).toInt())

    //Lower 32 bits are row id
    public val row: Int get() = id.toInt()

    public companion object {
        public fun of(archetype: Archetype, row: Int): Record =
            Record((archetype.id.toLong() shl 32) or row.toLong())
    }
}
