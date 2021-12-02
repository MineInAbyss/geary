package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.accessors.GearyAccessorScope
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.engine.GearyEngine
import kotlin.reflect.KClass

public fun Engine.entity(): GearyEntity = getNextId().toGeary()

public inline fun GearyEngine.entity(run: GearyAccessorScope.(entity: GearyEntity) -> Unit): GearyEntity {
    val entity = getNextId().toGeary()
    run(scope, entity)
    return entity
}

@Deprecated("TODO REIMPLEMENT")
public inline fun Engine.temporaryEntity(run: (GearyEntity) -> Unit) {
    TODO("NOT IMPLEMENTED")
}


@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
public fun componentId(id: KClass<out GearyComponentId>): Nothing = error("Trying to access id for component id")

@Deprecated("D:")


public fun GearyComponentId.getComponentInfo(): ComponentInfo? = TODO("")
//this.toGeary().get()