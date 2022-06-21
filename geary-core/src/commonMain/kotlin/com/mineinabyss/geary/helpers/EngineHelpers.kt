package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.annotations.optin.ExperimentalAsyncGearyAPI
import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.systems.GearySystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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

public inline fun <reified T> component(): GearyEntity = component(T::class)

public fun component(kClass: KClass<*>): GearyEntity = componentId(kClass).toGeary()

/** Gets or registers the id of a component of type [T] */
public inline fun <reified T> componentId(): GearyComponentId = componentId(T::class)

/** Gets or registers the id of a component of type [T], adding the [HOLDS_DATA] role if [T] is not nullable. */
public inline fun <reified T> componentIdWithNullable(): GearyComponentId =
    componentId<T>().withRole(if (typeOf<T>().isMarkedNullable) NO_ROLE else HOLDS_DATA)

/**
 * Gets the id of a component by its serial name.
 * Throws an error if the component name does not exist.
 */
public fun componentId(serialName: String): GearyComponentId =
    componentId(globalContext.serializers.getClassFor(serialName))

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

@ExperimentalAsyncGearyAPI
public inline fun <T> runSafely(
    scope: CoroutineScope = globalContext.engine,
    crossinline run: suspend () -> T
): Deferred<T> {
    val deferred = globalContext.engine.async(start = CoroutineStart.LAZY) { run() }
    globalContext.engine.runSafely(scope, deferred)
    return deferred
}
