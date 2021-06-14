package com.mineinabyss.geary.ecs.helper

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlin.reflect.KClass

public fun Collection<String>.toComponentClasses(): Collection<KClass<out GearyComponent>> =
    map { Formats.getClassFor(it) }
