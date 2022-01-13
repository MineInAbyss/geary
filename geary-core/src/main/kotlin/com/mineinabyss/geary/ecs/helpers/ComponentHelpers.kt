package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.serialization.Formats

/** Neatly lists all the components on this entity. */
public fun GearyEntity.listComponents(): String {
    return """
        Instance:
        ${getInstanceComponents().joinToString("\n") { "${it::class.simpleName}: $it" }}
        
        Persisting:
        ${getPersistingComponents().joinToString("\n") { "${Formats.getSerialNameFor(it::class)}: $it" }}
        """.trimIndent()
}
