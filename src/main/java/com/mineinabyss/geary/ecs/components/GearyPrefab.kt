package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.autoscan.ExcludeAutoscan
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.GearyPrefab.Companion.serializer
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * A prefab of sorts for entities. This is what we use to read config files to create entity types from. Since different
 * extensions may want to customize those config files, they must implement this class and add the functionality they
 * want.
 *
 * ## Instance/Persisting vs Static
 *
 * There is a distinction between instance/persisting, and static components here. All three component types will be
 * applied to a new entity created from this prefab. Static components are deserialized once on startup, and references
 * to the same objects are given to all subclasses of this entity. This is useful for immutable or shared data which
 * doesn't need to be copied for each new entity.
 *
 * Instance components however are deserialized every time to ensure a 100% unique deepcopy. These may persist as well,
 * or may just be added as temporary components
 *
 * ## Why is this a [GearyComponent]
 *
 * This class is serializable in two ways. Firstly we may use its [serializer()][serializer] when reading from a config.
 *
 * The class is also serializable as a [GearyComponent] and is registered into the ECS in this way. This means when
 * we can persist an entity type directly on an entity as though it's a component, in which case it will be serialized
 * as a string reference `plugin:typename`, which it then uses to find the entity type in [PrefabManager].
 */
@Serializable
@SerialName("geary:prefab")
@ExcludeAutoscan
public class GearyPrefab(
    @SerialName("instance")
    private val instanceComponents: Set<@Polymorphic GearyComponent> = setOf(),
    @SerialName("persisting")
    private val persistingComponents: Set<@Polymorphic GearyComponent> = setOf(),
    @SerialName("static")
    private val staticComponents: Set<@Polymorphic GearyComponent> = setOf(),
) {
    @Serializable
    private data class ComponentDeepCopy(
        val instance: Set<@Polymorphic GearyComponent>,
        val persist: Set<@Polymorphic GearyComponent>
    )

    //TODO this is the safest and cleanest way to deepcopy. Check how this performs vs deepcopy's reflection method.
    private val serializedComponents by lazy {
        Formats.cborFormat.encodeToByteArray(
            ComponentDeepCopy.serializer(),
            ComponentDeepCopy(instanceComponents, persistingComponents)
        )
    }

    private val deepCopied
        get() = Formats.cborFormat.decodeFromByteArray(ComponentDeepCopy.serializer(), serializedComponents)

    public fun decodeComponentsTo(entity: GearyEntity) {
        val (instance, persist) = deepCopied
        //order of addition determines which group overrides which
        entity.setAll(staticComponents + instance + this)
        entity.setAllPersisting(persist)
    }

    public fun instantiatePersistingComponents(): Set<GearyComponent> = deepCopied.persist + this
}
