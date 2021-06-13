package com.mineinabyss.geary.ecs.helper

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlin.reflect.KClass

public fun mapComponentNamesToClasses(names: Collection<String>): Collection<KClass<out GearyComponent>> =
    names.map { Formats.getClassFor(it) }
