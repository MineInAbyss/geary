package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.components.ComponentInfo
import kotlin.reflect.KClass

public fun Engine.entity(): GearyEntity = getNextId().toGeary()

public inline fun Engine.entity(run: GearyEntity.() -> Unit): GearyEntity = getNextId().toGeary(run)

/** Creates an entity that will get removed once [run] completes or fails. */
public inline fun Engine.temporaryEntity(run: (GearyEntity) -> Unit) {
    val entity = getNextId().toGeary()
    try {
        run(entity)
    } catch (e: Throwable) {
        e.printStackTrace()
    } finally {
        entity.removeEntity()
    }
}

public inline fun <reified T> componentId(): GearyComponentId = componentId(T::class)

@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
public fun componentId(id: KClass<out GearyComponentId>): Nothing = error("Trying to access id for component id")

public fun componentId(kClass: KClass<*>): GearyComponentId = Engine.getOrRegisterComponentIdForClass(kClass)

public fun GearyComponentId.getComponentInfo(): ComponentInfo? = this.toGeary().get()

public val GearyEntity.type: GearyType get() = Engine.getType(id)
