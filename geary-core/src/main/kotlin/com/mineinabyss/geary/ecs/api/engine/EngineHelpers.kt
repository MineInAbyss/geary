package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.serialization.Formats
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.mp.KoinPlatformTools
import kotlin.reflect.KClass
import kotlin.reflect.KType

/** Creates a new empty entity. May reuse recently deleted entity ids. */
public fun KoinComponent.entity(): GearyEntity = get<Engine>().newEntity()

/** @see entity */
public inline fun KoinComponent.entity(run: GearyEntity.() -> Unit): GearyEntity = entity().apply(run)

/** Creates a new empty entity that will get removed once [run] completes or fails. */
public inline fun Engine.temporaryEntity(run: (GearyEntity) -> Unit) {
    val entity = newEntity()
    try {
        run(entity)
    } catch (e: Throwable) {
        e.printStackTrace()
    } finally {
        entity.removeEntity()
    }
}

public inline fun KoinComponent.temporaryEntity(run: (GearyEntity) -> Unit) {
    get<Engine>().temporaryEntity(run)
}

/** Gets or registers the id of a component of type [T] */
public inline fun <reified T> KoinComponent.componentId(): GearyComponentId = componentId(T::class)

/**
 * Gets the id of a component by its serial name.
 * Throws an error if the component name does not exist.
 */
public fun KoinComponent.componentId(serialName: String): GearyComponentId =
    componentId(Formats.getClassFor(serialName))

/** Gets or registers the id of a component by its [kType]. */
public fun KoinComponent.componentId(kType: KType): GearyComponentId = componentId(kType.classifier as KClass<*>)

/** Gets or registers the id of a component by its [kClass]. */
public fun KoinComponent.componentId(kClass: KClass<*>): GearyComponentId = get<Engine>().getOrRegisterComponentIdForClass(kClass)

@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
@Suppress("UNUSED_PARAMETER")
public fun KoinComponent.componentId(kClass: KClass<out GearyComponentId>): Nothing = error("Trying to access id for component id")

/** Gets the [ComponentInfo] component from a component's id. */
public fun GearyComponentId.getComponentInfo(): ComponentInfo? = this.toGeary().get()

@Deprecated("This will be replaced with multi-receiver access in Kotlin 1.6.20")
public val globalEngine: Engine get() = KoinPlatformTools.defaultContext().get().get()
