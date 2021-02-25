package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntityId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import kotlin.reflect.KClass

public inline fun Engine.entity(run: GearyEntity.() -> Unit): GearyEntity = geary(getNextId(), run)

public inline fun <reified T> componentId(): GearyEntityId = componentId(T::class)

public fun componentId(component: GearyComponent): GearyEntityId = componentId(component::class)

public fun componentId(kClass: KClass<*>): GearyEntityId = Engine.getComponentIdForClass(kClass)
