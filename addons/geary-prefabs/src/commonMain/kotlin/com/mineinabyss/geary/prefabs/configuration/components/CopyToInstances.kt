package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:copy_to_instances
 *
 * A component prefabs may use to specify a list of components which should be copied to instances of itself.
 *
 * These components are not active on the prefab itself and will be instantiated for every new instance of this class.
 * Components may be defined as simply [temporary] or [persisting], where persisting components will be added as such
 * in the Engine.
 */
@Serializable
@SerialName("geary:copy_to_instances")
data class CopyToInstances(
    private val temporary: Set<@Polymorphic Component> = setOf(),
    private val persisting: Set<@Polymorphic Component> = setOf(),
) {
    val formats get() = serializableComponents.formats

    // This is the safest and cleanest way to deep-copy, even if a little performance intense.
    private val serializedComponents by lazy { formats.binaryFormat.encodeToByteArray(serializer(), this) }

    private fun getDeepCopied() = formats.binaryFormat.decodeFromByteArray(serializer(), serializedComponents)

    fun decodeComponentsTo(entity: Entity, override: Boolean = true) {
        val (instance, persist) = getDeepCopied()
        //order of addition specifies that persisting components should override all
        entity.setAll(instance, override)
        entity.setAllPersisting(persist, override)
    }
}
