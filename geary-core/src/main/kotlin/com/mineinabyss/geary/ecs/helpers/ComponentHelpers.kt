package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.api.FormatsContext
import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity

/** Neatly lists all the components on this entity. */
context(EngineContext, FormatsContext)
public fun GearyEntity.listComponents(): String {
    return """
        Instance:
        ${getInstanceComponents().joinToString("\n") { "${it::class.simpleName}: $it" }}
        
        Persisting:
        ${getPersistingComponents().joinToString("\n") { "${formats.getSerialNameFor(it::class)}: $it" }}
        """.trimIndent()
}
