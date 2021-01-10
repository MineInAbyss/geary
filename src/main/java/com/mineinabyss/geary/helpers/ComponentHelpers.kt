package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.getInstanceComponents
import com.mineinabyss.geary.ecs.components.getPersistingComponents
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.type

/** Gets the serial name of this component as registered in [Formats] */
public val GearyComponent.serialName: String?
    get() = Formats.cborFormat.serializersModule.getPolymorphic(GearyComponent::class, this)?.descriptor?.serialName

private val Collection<GearyComponent>.names: String get() = mapNotNull { it.serialName }.joinToString()

/** Neatly lists all the components on this entity. */
public fun GearyEntity.listComponents(): String {
    return """
        Static: ${type?.staticComponentMap?.values?.names}
        Instance: ${getInstanceComponents().names}
        Persisting: ${getPersistingComponents().names}
    """.trimIndent()
}
