package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import kotlin.reflect.KClass

internal typealias ComponentClass = KClass<out GearyComponent>
