package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import org.bukkit.NamespacedKey

public inline fun <reified T : GearyComponent> Formats.getNamespacedKeyFor(): NamespacedKey? =
    getSerialNameFor<T>()?.toMCKey()?.addComponentPrefix()
