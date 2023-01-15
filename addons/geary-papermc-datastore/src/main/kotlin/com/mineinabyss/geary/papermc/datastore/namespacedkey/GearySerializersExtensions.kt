package com.mineinabyss.geary.papermc.datastore.namespacedkey

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.ComponentSerializers
import kotlinx.serialization.DeserializationStrategy
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import kotlin.reflect.KClass

/** Gets the [NamespacedKey] for this component which should be used when encoding with a [PersistentDataContainer] */
inline fun <reified T : GearyComponent> ComponentSerializers.getNamespacedKeyFor(): NamespacedKey? =
    getSerialNameFor(T::class)?.toComponentKey()

/** Gets the serializer for a [NamespacedKey][key] registered for polymorphic serialization under [baseClass]. */
fun <T : GearyComponent> ComponentSerializers.getSerializerForNamespaced(
    key: NamespacedKey,
    baseClass: KClass<in T> = GearyComponent::class
): DeserializationStrategy<out T>? =
    getSerializerFor(key.toSerialName(), baseClass)
