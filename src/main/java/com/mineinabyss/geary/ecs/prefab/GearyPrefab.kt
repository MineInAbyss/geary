package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.autoscan.ExcludeAutoscan
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.components.addPersistingComponents
import com.mineinabyss.geary.ecs.engine.ComponentClass
import com.mineinabyss.geary.ecs.prefab.GearyPrefab.Companion.serializer
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
@SerialName("geary:type")
@ExcludeAutoscan
public class GearyPrefab(
    public val spawn: @Polymorphic GearyComponent? = null,
    private val instanceComponents: Set<@Polymorphic GearyComponent> = setOf(),
    private val persistingComponents: Set<@Polymorphic GearyComponent> = setOf(),
    private val staticComponents: Set<@Polymorphic GearyComponent> = setOf(),
) {
    public val name: String by lazy { PrefabManager.getNameForPrefab(this) }

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
        entity.addComponents(staticComponents + instance + this)
        entity.addPersistingComponents(persist)
    }

    public fun instantiatePersistingComponents(): Set<GearyComponent> = deepCopied.persist + this

    public val staticComponentMap: Map<ComponentClass, GearyComponent> by lazy {
        staticComponents.associateBy { it::class }
    }

    /** Gets a static component of type [T] from this entity type. */
    public inline fun <reified T : GearyComponent> get(): T? = staticComponentMap[T::class] as? T

    /** Checks whether this entity type has a static component of type [T]. */
    public inline fun <reified T : GearyComponent> has(): Boolean = staticComponentMap.containsKey(T::class)
}
