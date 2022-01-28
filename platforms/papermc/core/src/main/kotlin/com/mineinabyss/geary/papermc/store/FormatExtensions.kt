package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.DeserializationStrategy
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import kotlin.reflect.KClass

/** Gets the [NamespacedKey] for this component which should be used when encoding with a [PersistentDataContainer] */
public inline fun <reified T : GearyComponent> Formats.getNamespacedKeyFor(): NamespacedKey? =
    getSerialNameFor<T>()?.toComponentKey()

/** Gets the serializer for a [NamespacedKey][key] registered for polymorphic serialization under [baseClass]. */
public fun Formats.getSerializerFor(
    key: NamespacedKey,
    baseClass: KClass<*> = GearyComponent::class
): DeserializationStrategy<out GearyComponent>? =
    getSerializerFor(key.toSerialName(), baseClass)
