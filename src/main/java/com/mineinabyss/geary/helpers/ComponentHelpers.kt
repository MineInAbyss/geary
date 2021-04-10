package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.components.PrefabKey
import com.mineinabyss.geary.ecs.serialization.Formats

/** Gets the serial name of this component as registered in [Formats] */
public val GearyComponent.serialName: String?
    get() = Formats.cborFormat.serializersModule.getPolymorphic(GearyComponent::class, this)?.descriptor?.serialName

private val Collection<GearyComponent>.names: String get() = mapNotNull { it.serialName }.joinToString()

/** Neatly lists all the components on this entity. */
public fun GearyEntity.listComponents(): String {
    return """
        Type: ${type.mapNotNull { geary(it).get<PrefabKey>() }}
        Instance: ${getInstanceComponents().names}
        Persisting: ${getPersistingComponents().names}
    """.trimIndent()
}
