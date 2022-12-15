package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.papermc.store.toComponentKey
import com.mineinabyss.geary.papermc.store.toSerialName
import com.mineinabyss.geary.serialization.GearySerializers
import kotlinx.serialization.DeserializationStrategy
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import kotlin.reflect.KClass

/** Gets the [NamespacedKey] for this component which should be used when encoding with a [PersistentDataContainer] */
inline fun <reified T : GearyComponent> GearySerializers.getNamespacedKeyFor(): NamespacedKey? =
    getSerialNameFor(T::class)?.toComponentKey()

/** Gets the serializer for a [NamespacedKey][key] registered for polymorphic serialization under [baseClass]. */
fun <T: GearyComponent> GearySerializers.getSerializerFor(
    key: NamespacedKey,
    baseClass: KClass<in T> = GearyComponent::class
): DeserializationStrategy<out T>? =
    getSerializerFor(key.toSerialName(), baseClass)
