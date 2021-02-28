package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import kotlin.reflect.KClass


public inline fun <reified T : GearyComponent> accessor(): Accessor<T> = Accessor(T::class)

public open class Accessor<T : GearyComponent>(internal val kClass: KClass<T>)
