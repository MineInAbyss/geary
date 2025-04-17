package com.mineinabyss.geary.serialization.serializers

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.ComponentSerializers
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

typealias SerializableComponentId = @Contextual ComponentId

class ComponentIdSerializer(
    val componentSerializers: ComponentSerializers,
    val world: Geary,
) : KSerializer<SerializableComponentId> {
    override val descriptor = PrimitiveSerialDescriptor("EventComponent", PrimitiveKind.STRING)

    private val polymorphicListAsMapSerializer = PolymorphicListAsMapSerializer.ofComponents()

    override fun deserialize(decoder: Decoder): SerializableComponentId {
        return world.componentId(getComponent(decoder.decodeString(), decoder.serializersModule))
    }

    override fun serialize(encoder: Encoder, value: SerializableComponentId) {
        TODO()
    }

    fun getComponent(name: String, module: SerializersModule): KClass<out Component> {
        val namespaces = polymorphicListAsMapSerializer
            .getParentConfig(module)?.namespaces
            ?: emptyList()
        return componentSerializers.getClassFor(name, namespaces)
    }
}
