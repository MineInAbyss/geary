package com.mineinabyss.geary.ecs.types

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.components.addPersistingComponents
import com.mineinabyss.geary.ecs.engine.ComponentClass
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.types.GearyEntityType.Companion.serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

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
 * as a string reference `plugin:typename`, which it then uses to find the entity type in [EntityTypeManager].
 */
@Serializable
@SerialName("geary:type")
public abstract class GearyEntityType : GearyComponent {
    /** Resulting set will be added to the list of instance components, but won't be serialized. */
    protected open fun MutableSet<GearyComponent>.addComponents() {}

    /** Resulting set will be added to the list of persisting components and will be encoded to the entity's
     * [PersistentDataContainer] if applicable. */
    protected open fun MutableSet<GearyComponent>.addPersistingComponents() {}

    /** Resulting set will be added to the list of static components, but won't be serialized. */
    protected open fun MutableSet<GearyComponent>.addStaticComponents() {}

    @SerialName("instanceComponents")
    private val _instanceComponents = mutableSetOf<GearyComponent>()

    @SerialName("persistingComponents")
    private val _persistingComponents = mutableSetOf<GearyComponent>()

    @SerialName("staticComponents")
    private val _staticComponents = mutableSetOf<GearyComponent>()


    private val instanceComponents: Set<GearyComponent> by lazy {
        _instanceComponents.apply { addComponents() }.toSet()
    }

    private val persistingComponents: Set<GearyComponent> by lazy {
        _persistingComponents.apply { addPersistingComponents() }.toSet()
    }

    private val staticComponents: Set<GearyComponent> by lazy {
        _staticComponents.apply { addStaticComponents() }.toSet()
    }

    protected abstract val types: GearyEntityTypes<*>

    @Transient
    public lateinit var name: String
        internal set

    @Serializable
    private data class ComponentDeepCopy(
        val instance: Set<GearyComponent>,
        val persist: Set<GearyComponent>
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

    //TODO REFACTOR
//    public fun encodeComponentsTo(pdc: PersistentDataContainer) {
//        pdc.encodeComponents(deepCopied.persist + this)
//    }

    public val staticComponentMap: Map<ComponentClass, GearyComponent> by lazy {
        staticComponents.associateBy { it::class }
    }

    /** Gets a static component of type [T] from this entity type. */
    public inline fun <reified T : GearyComponent> get(): T? = staticComponentMap[T::class] as? T

    /** Checks whether this entity type has a static component of type [T]. */
    public inline fun <reified T : GearyComponent> has(): Boolean = staticComponentMap.containsKey(T::class)

    /**
     * Allows us to serialize entity types to a reference to ones actually registered in the system.
     * This is used to load the static entity type when we decode components from an in-game entity.
     */
    public class ByReferenceSerializer<T : GearyEntityType>(
        kclass: KClass<T>
    ) : KSerializer<T> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("geary:${kclass.simpleName!!}".toLowerCase(), PrimitiveKind.STRING)

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(decoder: Decoder): T {
            val (plugin, type) = decoder.decodeString().split(':')
            return (EntityTypeManager[plugin, type]
                ?: error("Type $plugin:$type not found while deserializing")) as? T
                ?: error("Found an entity type with the same name but different class. Were conflicting ones registered?")
        }

        override fun serialize(encoder: Encoder, value: T) {
            encoder.encodeString("${value.types.plugin}:${value.name}")
        }
    }
}
