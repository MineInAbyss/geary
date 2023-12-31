package com.mineinabyss.geary.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ProvidedNamespaces(val namespaces: List<String>) : KSerializer<ProvidedNamespaces> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("ProvidedNamespaces", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): ProvidedNamespaces {
        error("")
    }

    override fun serialize(encoder: Encoder, value: ProvidedNamespaces) {
        error("")
    }
}
