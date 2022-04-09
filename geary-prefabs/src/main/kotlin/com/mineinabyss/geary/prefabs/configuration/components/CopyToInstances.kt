package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.helpers.GearyContextKoin
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
public data class CopyToInstances(
    private val temporary: Set<@Polymorphic GearyComponent> = setOf(),
    private val persisting: Set<@Polymorphic GearyComponent> = setOf(),
): GearyContext by GearyContextKoin() {
    // This is the safest and cleanest way to deep-copy, even if a little performance intense.
    private val serializedComponents by lazy {
        formats.cborFormat.encodeToByteArray(serializer(), this)
    }

    private fun getDeepCopied() = formats.cborFormat.decodeFromByteArray(serializer(), serializedComponents)

    public fun decodeComponentsTo(entity: GearyEntity, override: Boolean = true) {
        val (instance, persist) = getDeepCopied()
        //order of addition specifies that persisting components should override all
        entity.setAll(instance, override)
        entity.setAllPersisting(persist, override)
    }
}
