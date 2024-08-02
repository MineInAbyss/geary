package com.mineinabyss.geary.serialization.serializers

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.serialization.serializableComponents
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

@Serializable(with = SerializableComponentId.Serializer::class)
class SerializableComponentId(val id: ComponentId) {
    object Serializer : KSerializer<SerializableComponentId> {
        override val descriptor = PrimitiveSerialDescriptor("EventComponent", PrimitiveKind.STRING)

        private val polymorphicListAsMapSerializer = PolymorphicListAsMapSerializer.ofComponents()

        override fun deserialize(decoder: Decoder): SerializableComponentId {
            return SerializableComponentId(componentId(getComponent(decoder.decodeString(), decoder.serializersModule)))
        }

        override fun serialize(encoder: Encoder, value: SerializableComponentId) {
            TODO()
        }

        fun getComponent(name: String, module: SerializersModule): KClass<out Component> {
            val namespaces = polymorphicListAsMapSerializer
                .getParentConfig(module)?.namespaces
                ?: emptyList()
            return serializableComponents.serializers.getClassFor(name, namespaces)
        }
    }
}
