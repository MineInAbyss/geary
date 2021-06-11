package com.mineinabyss.geary.ecs.helper

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlin.reflect.KClass

public class Components (names: Collection<String>) {
    public val classes: Collection<KClass<out GearyComponent>> by lazy { names.map { Formats.getClassFor(it) } }
}