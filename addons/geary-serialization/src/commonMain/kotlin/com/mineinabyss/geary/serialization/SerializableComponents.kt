package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.serialization.components.Persists
import com.mineinabyss.geary.serialization.dsl.builders.ComponentSerializersBuilder
import com.mineinabyss.geary.serialization.dsl.builders.FormatsBuilder
import com.mineinabyss.geary.serialization.formats.Format
import com.mineinabyss.geary.serialization.formats.Formats
import com.mineinabyss.geary.serialization.serializers.ComponentIdSerializer
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.*
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass

data class SerializableComponentsBuilder(
    val world: Geary,
    val serializers: ComponentSerializersBuilder = ComponentSerializersBuilder(),
    val formats: FormatsBuilder = FormatsBuilder(),
    var overridePersists: ComponentId? = null,
    var addGearyEntitySerializer: Boolean = true,
) {
    /** Adds a [SerializersModule] for polymorphic serialization of [Component]s within the ECS. */
    inline fun components(crossinline init: PolymorphicModuleBuilder<Component>.() -> Unit) {
        module { polymorphic(Component::class) { init() } }
    }

    fun format(ext: String, format: (SerializersModule) -> Format) {
        formats.register(ext, format)
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    inline fun <reified T : Component> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
        component(T::class, serializer)
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    @OptIn(InternalSerializationApi::class)
    fun <T : Component> PolymorphicModuleBuilder<T>.component(
        subclass: KClass<T>,
        serializer: KSerializer<T> = subclass.serializerOrNull()
            ?: error("No serializer found for $subclass while registering serializable component"),
    ) {
        val serialName = serializer.descriptor.serialName
        if (serializers.serialNameToClass.containsKey(serialName)) {
            error("A component with serial name $serialName is already registered")
        }
        serializers.serialNameToClass[serialName] = subclass
        subclass(subclass, serializer)
    }

    inline fun <reified T : Any> namedComponent(name: String) {
        serializers.serialNameToClass[name] = T::class
    }

    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
    inline fun module(init: SerializersModuleBuilder.() -> Unit) {
        serializers.modules += SerializersModule { init() }
    }

    fun build(): SerializableComponentsModule {
        module {
            println("Adding Geary to serializers")
            contextual<Geary>(GearyWorldProvider(world))
            contextual<ComponentId>(ComponentIdSerializer(serializers.build(), world))
        }
        val serializers = serializers.build()
        if (addGearyEntitySerializer) {
            components { component(GearyEntitySerializer()) }
        }
        return SerializableComponentsModule(
            serializers = serializers,
            formats = formats.build(serializers),
            persists = overridePersists ?: world.componentId<Persists>()
        )
    }
}

data class SerializableComponentsModule(
    val serializers: ComponentSerializers,
    val formats: Formats,
    val persists: ComponentId,
)

val SerializableComponents = createAddon<SerializableComponentsBuilder, SerializableComponentsModule>(
    "Serializable Components",
    { SerializableComponentsBuilder(this) }
) { configuration.build() }

@GearyDSL
fun GearySetup.serialization(configure: SerializableComponentsBuilder.() -> Unit) =
    install(SerializableComponents, configure)

fun SerializersModule.getWorld(): Geary = (getContextual(Geary::class) as GearyWorldProvider).world

class GearyWorldProvider(val world: Geary): KSerializer<Geary> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    override fun deserialize(decoder: Decoder): Geary {
        return world
    }

    override fun serialize(encoder: Encoder, value: Geary) {
    }
}
