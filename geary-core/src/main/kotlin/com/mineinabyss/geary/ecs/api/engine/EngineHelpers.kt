package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.accessors.GearyAccessorScope
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.components.ComponentInfo
import kotlin.reflect.KClass

public fun Engine.entity(): GearyEntity = getNextId().toGeary()

public inline fun Engine.entity(run: GearyAccessorScope.(entity: GearyEntity) -> Unit): GearyEntity =
    getNextId().toGeary(run)

@Deprecated("TODO REIMPLEMENT")
public inline fun Engine.temporaryEntity(run: (GearyEntity) -> Unit) {
    TODO("NOT IMPLEMENTED")
}

@Deprecated("D:")
public inline fun <reified T> componentId(): GearyComponentId = TODO("")

//componentId(T::class)

@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
public fun componentId(id: KClass<out GearyComponentId>): Nothing = error("Trying to access id for component id")

@Deprecated("D:")
public fun componentId(kClass: KClass<*>): GearyComponentId = TODO("")
//Engine.getOrRegisterComponentIdForClass(kClass)

public fun GearyComponentId.getComponentInfo(): ComponentInfo? = TODO("")
//this.toGeary().get()

@Deprecated("D:")
public val GearyEntity.type: GearyType
    get() = TODO("")
//Engine.getType(id)
