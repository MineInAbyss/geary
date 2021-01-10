package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats.cborFormat
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.geary
import com.mineinabyss.geary.minecraft.isGearyEntity
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.BYTE_ARRAY

public inline fun <reified T : GearyComponent> PersistentDataContainer.encode(
        serializer: SerializationStrategy<T> = cborFormat.serializersModule.serializer(),
        key: String = T::class.qualifiedName ?: error(""),
        value: T
) {
    val encoded = cborFormat.encodeToByteArray(serializer, value)
    this[NamespacedKey(geary, key), BYTE_ARRAY] = encoded
}

//TODO make others pass plugin here
public inline fun <reified T : GearyComponent> PersistentDataContainer.decode(
        serializer: DeserializationStrategy<out T> = cborFormat.serializersModule.serializer(),
        key: NamespacedKey
): T? {
    val encoded = this[key, BYTE_ARRAY] ?: return null
    return cborFormat.decodeFromByteArray(serializer, encoded)
}

public fun PersistentDataContainer.encodeComponents(components: Collection<GearyComponent>) {
    isGearyEntity = true
    //remove all keys present on the PDC so we only end up with the new list of components being encoded
    keys.filter { it.namespace == "geary" && it != SpigotEngine.componentsKey }.forEach { remove(it) }

    //get the serializer registered under the MobzyComponent class through polymorphic serialization, and use it to
    // write a serialized value under its serialname
    for (value in components) {
        val serializer = cborFormat.serializersModule.getPolymorphic(GearyComponent::class, value)
                ?: continue //TODO error?
        encode(serializer, serializer.descriptor.serialName.toMCKey(), value)
    }
}

public fun PersistentDataContainer.decodeComponents(): Set<GearyComponent> {
    //key is serialname, we find all the valid ones registered in our module and use those serializers to deserialize
    return keys.mapNotNull { key ->
        val serializer = cborFormat.serializersModule.getPolymorphic(GearyComponent::class, key.key.toSerialKey())
                ?: return@mapNotNull null
        decode(serializer, key)
    }.toSet()
}

private fun String.toMCKey() = replace(":", "_")
private fun String.toSerialKey() = replace("_", ":")

