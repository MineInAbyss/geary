package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
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
        val persisting: List<@Polymorphic Component>?
    )

    val formats get() = serializableComponents.formats

    // This is the safest and cleanest way to deep-copy, even if a little performance intense.
    private val serializedComponents by lazy {
        formats.binaryFormat.encodeToByteArray(
            DeepCopy.serializer(),
            DeepCopy(temporary, persisting)
        )
    }

    private fun getDeepCopied() = formats.binaryFormat.decodeFromByteArray(
        DeepCopy.serializer(), serializedComponents
    )

    fun decodeComponentsTo(entity: Entity) {
        val (instance, persist) = getDeepCopied()
        //order of addition specifies that persisting components should override all
        if (instance != null) {
            entity.setAll(instance, override = false)
        }
        if (persist != null) {
            entity.setAllPersisting(persist, override = false)
        }
    }
}

