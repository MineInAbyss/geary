package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.DeserializationStrategy
import org.bukkit.NamespacedKey
import kotlin.reflect.KClass

public inline fun <reified T : GearyComponent> Formats.getNamespacedKeyFor(): NamespacedKey? =
    getSerialNameFor<T>()?.toMCKey()?.addComponentPrefix()

public fun Formats.getSerializerFor(
    key: NamespacedKey,
    baseClass: KClass<*> = GearyComponent::class
): DeserializationStrategy<out GearyComponent>? =
    getSerializerFor(key.toSerialName(), baseClass)
