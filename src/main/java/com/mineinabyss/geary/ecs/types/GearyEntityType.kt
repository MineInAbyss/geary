package com.mineinabyss.geary.ecs.types

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.ecs.components.addPersistingComponents
import com.mineinabyss.geary.ecs.engine.ComponentClass
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.minecraft.store.encodeComponents
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.persistence.PersistentDataContainer

@Serializable(with = GearyEntityType.Serializer::class)
@SerialName("geary:type")
public abstract class GearyEntityType : GearyComponent() {
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

    private val deepCopied get() = Formats.cborFormat.decodeFromByteArray(ComponentDeepCopy.serializer(), serializedComponents)


    public fun decodeComponentsTo(entity: GearyEntity) {
        val (instance, persist) = deepCopied
        //order of addition determines which group overrides which
        entity.addComponents(staticComponents + instance + this)
        entity.addPersistingComponents(persist)
    }

    public fun encodeComponentsTo(pdc: PersistentDataContainer) {
        pdc.encodeComponents(deepCopied.persist + this)
    }

    public val staticComponentMap: Map<ComponentClass, GearyComponent> by lazy {
        staticComponents.associateBy { it::class }
    }

    public inline fun <reified T : GearyComponent> get(): T? = staticComponentMap[T::class] as? T

    public inline fun <reified T : GearyComponent> has(): Boolean = staticComponentMap.containsKey(T::class)

    public class Serializer : KSerializer<GearyEntityType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("entitytype", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): GearyEntityType {
            val (plugin, type) = decoder.decodeString().split(':')
            return EntityTypeManager[plugin, type] ?: error("Type: $plugin:$type not found while deserializing")
        }

        override fun serialize(encoder: Encoder, value: GearyEntityType) {
            encoder.encodeString("${value.types.plugin}:${value.name}")
        }
    }
}
