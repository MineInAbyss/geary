package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats

/** Gets the serial name of this component as registered in [Formats] */
public val GearyComponent.serialName: String?
    get() = Formats.cborFormat.serializersModule.getPolymorphic(GearyComponent::class, this)?.descriptor?.serialName

private val Collection<GearyComponent>.names: String get() = mapNotNull { it.serialName }.joinToString()

//TODO reimplement
/** Neatly lists all the components on this entity. */
//public fun GearyEntity.listComponents(): String {
//    return """
//        Type: ${type.mapNotNull { it.toGeary().get<PrefabKey>() }}
//        Instance: ${getInstanceComponents().names}
//        Persisting: ${getPersistingComponents().names}
//    """.trimIndent()
//}
