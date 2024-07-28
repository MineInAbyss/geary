package com.mineinabyss.geary.actions.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal object DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Time", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Duration) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Duration {
        val string = decoder.decodeString()
        return Duration.parseOrNull(string) ?: fromString(decoder.decodeString()) ?: error("Not a valid duration: $string")
    }

    private fun fromString(string: String): Duration? {
        val splitAt = string.indexOfFirst { it.isLetter() }.takeIf { it > 0 } ?: string.length
        val value = string.take(splitAt).toDouble()
        return when (string.drop(splitAt)) {
            "ms" -> value.milliseconds
            "s" -> value.seconds
            "m" -> value.minutes
            "h" -> value.hours
            "d" -> value.days
            "w" -> value.days * 7
            "mo" -> value.days * 31
            else -> null
        }
    }
}
