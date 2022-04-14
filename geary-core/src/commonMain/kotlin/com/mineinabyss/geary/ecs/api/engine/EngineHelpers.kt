package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.context.globalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.reflect.KClass
import kotlin.reflect.KType

/** Creates a new empty entity. May reuse recently deleted entity ids. */
public fun entity(): GearyEntity = globalContext.engine.newEntity()

/** @see entity */
public inline fun entity(run: GearyEntity.() -> Unit): GearyEntity = entity().apply(run)

/** Creates a new empty entity that will get removed once [run] completes or fails. */
public inline fun <T> temporaryEntity(
    callRemoveEvent: Boolean = false,
    run: (GearyEntity) -> T
): T {
    val entity = entity()
    return try {
        run(entity)
    } finally {
        entity.removeEntity(callRemoveEvent)
    }
}

/** Gets or registers the id of a component of type [T] */
public inline fun <reified T> componentId(): GearyComponentId = componentId(T::class)

/**
 * Gets the id of a component by its serial name.
 * Throws an error if the component name does not exist.
 */
public fun componentId(serialName: String): GearyComponentId =
    componentId(globalContext.formats.getClassFor(serialName))

/** Gets or registers the id of a component by its [kType]. */
public fun componentId(kType: KType): GearyComponentId =
    componentId(kType.classifier as KClass<*>)

/** Gets or registers the id of a component by its [kClass]. */
public fun componentId(kClass: KClass<*>): GearyComponentId =
    globalContext.engine.getOrRegisterComponentIdForClass(kClass)

@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
@Suppress("UNUSED_PARAMETER")
public fun componentId(kClass: KClass<out GearyComponentId>): Nothing =
    error("Trying to access id for component id")

/** Gets the [ComponentInfo] component from a component's id. */
public fun GearyComponentId.getComponentInfo(): ComponentInfo? =
    this.toGeary().get()

public fun systems(vararg systems: GearySystem): List<Deferred<Unit>> {
    return systems.map { globalContext.engine.async { globalContext.engine.addSystem(it) } }
}

//TODO inline when compiler fixed
public /*inline*/ fun <T> runSafely(
    scope: CoroutineScope = globalContext.engine, /*crossinline*/
    run: suspend () -> T
): Deferred<T> {
    val deferred = globalContext.engine.async(start = CoroutineStart.LAZY) { run() }
    globalContext.engine.runSafely(scope, deferred)
    return deferred
}
