package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.getInstanceComponents
import com.mineinabyss.geary.ecs.components.getPersistingComponents
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.type

public val GearyComponent.serialName: String?
    get() = Formats.cborFormat.serializersModule.getPolymorphic(GearyComponent::class, this)?.descriptor?.serialName

public val Collection<GearyComponent>.names: String get() = mapNotNull { it.serialName }.joinToString()

public fun GearyEntity.listComponents(): String {
    return """
        Static: ${type?.staticComponentMap?.values?.names}
        Instance: ${getInstanceComponents().names}
        Persisting: ${getPersistingComponents().names}
    """.trimIndent()
}
