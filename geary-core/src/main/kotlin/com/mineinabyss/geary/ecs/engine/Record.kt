package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.EngineScope
import org.koin.core.component.get

@JvmInline
public value class Record(public val id: Long) : EngineScope {
    //TODO multiple receivers
    override val engine: Engine get() = get()

    //Upper 32 bits are row id
    public val archetype: Archetype get() = engine.getArchetype((id shr 32).toInt())

    //Lower 32 bits are row id
    public val row: Int get() = id.toInt()

    public companion object {
        public fun of(archetype: Archetype, row: Int): Record =
            Record((archetype.id.toLong() shl 32) or row.toLong())
    }
}
