package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.serialization.serializableComponents
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SerializableComponentId.Serializer::class)
class SerializableComponentId(val id: ComponentId) {
    object Serializer : KSerializer<SerializableComponentId> {
        override val descriptor = PrimitiveSerialDescriptor("EventComponent", PrimitiveKind.STRING)

        private val polymorphicListAsMapSerializer = PolymorphicListAsMapSerializer.ofComponents()

        override fun deserialize(decoder: Decoder): SerializableComponentId {
            val type = decoder.decodeString()
            val namespaces = polymorphicListAsMapSerializer
                .getParentConfig(decoder.serializersModule)?.namespaces
                ?: emptyList()
            val typeComponentId = componentId(serializableComponents.serializers.getClassFor(type, namespaces))
            return SerializableComponentId(typeComponentId)
        }

        override fun serialize(encoder: Encoder, value: SerializableComponentId) {
            TODO()
        }
    }
}
