package com.mineinabyss.geary.minecraft.access

import com.mineinabyss.geary.ecs.GearyComponent
import org.bukkit.entity.Entity


//TODO add the rest of the GearyEntity operations here
public inline fun <reified T : GearyComponent> Entity.get(): T? = gearyOrNull(this)?.get()

public inline fun <reified T : GearyComponent> Entity.with(let: (T) -> Unit): Unit? =
    gearyOrNull(this)?.get<T>()?.let(let)

public inline fun <reified T : GearyComponent> Entity.has(): Boolean = gearyOrNull(this)?.has<T>() ?: false
