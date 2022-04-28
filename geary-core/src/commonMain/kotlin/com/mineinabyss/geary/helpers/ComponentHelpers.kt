package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.GearyEntity

/** Neatly lists all the components on this entity. */
public fun GearyEntity.listComponents(): String {
    return """Instance:
        ${getInstanceComponents().joinToString("\n") { "${it::class.simpleName}: $it" }}
        
        Persisting:
        ${getPersistingComponents().joinToString("\n") { "${globalContext.serializers.getSerialNameFor(it::class)}: $it" }}
        """.trimIndent()
}
