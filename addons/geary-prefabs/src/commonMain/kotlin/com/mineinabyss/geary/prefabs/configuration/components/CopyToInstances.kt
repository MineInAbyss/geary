package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import com.mineinabyss.geary.serialization.setAllPersisting
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A component prefabs may use to specify a list of components which should be copied to instances of itself.
 *
 * These components are not active on the prefab itself and will be instantiated for every new instance of this class.
 * Components may be defined as simply [temporary] or [persisting], where persisting components will be added as such
 * in the Engine.
 */
@Serializable
@SerialName("geary:copy_to_instances")
data class CopyToInstances(
    private val temporary: SerializedComponents? = null,
    private val persisting: SerializedComponents? = null,
) {
    @Serializable
    private data class DeepCopy(
        val temporary: List<@Polymorphic Component>?,
        val persisting: List<@Polymorphic Component>?,
    )

    fun decodeComponentsTo(entity: Entity) {
        val binaryFormat = entity.world.getAddon(SerializableComponents).formats.binaryFormat

        // This is the safest and cleanest way to deep-copy, even if a little performance intense.
        val encoded = binaryFormat.encodeToByteArray(DeepCopy.serializer(), DeepCopy(temporary, persisting))
        val (instance, persist) = binaryFormat.decodeFromByteArray(DeepCopy.serializer(), encoded)

        if (instance != null) {
            entity.setAll(instance, override = false)
        }
        if (persist != null) {
            entity.setAllPersisting(persist, override = false)
        }
    }
}

