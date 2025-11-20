package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.modules.Geary
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

class GearyWorldProvider(val world: Geary) : KSerializer<Geary> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    override fun deserialize(decoder: Decoder): Geary {
        return world
    }

    override fun serialize(encoder: Encoder, value: Geary) {
    }
}

fun SerializersModule.getWorld(): Geary = (getContextual(Geary::class) as GearyWorldProvider).world
