package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.components.events.SuppressRemoveEvent
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.systems.GearySystem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** Creates a new empty entity. May reuse recently deleted entity ids. */
fun entity(): Entity = geary.entityProvider.newEntity()

/** @see entity */
inline fun entity(run: Entity.() -> Unit): Entity = entity().apply(run)

/** Creates a new empty entity that will get removed once [run] completes or fails. */
inline fun <T> temporaryEntity(
    run: (Entity) -> T
): T {
    val entity = entity {
        add<SuppressRemoveEvent>()
    }
    return try {
        run(entity)
    } finally {
        entity.removeEntity()
    }
}

inline fun <reified T> component(): Entity = component(T::class)

fun component(kClass: KClass<*>): Entity = componentId(kClass).toGeary()

/** Gets or registers the id of a component of type [T] */
inline fun <reified T> componentId(): ComponentId = componentId(T::class)

/** Gets or registers the id of a component of type [T], adding the [HOLDS_DATA] role if [T] is not nullable. */
inline fun <reified T> componentIdWithNullable(): ComponentId =
    componentId<T>().withRole(if (typeOf<T>().isMarkedNullable) NO_ROLE else HOLDS_DATA)

/**
 * Gets the id of a component by its serial name.
 * Throws an error if the component name does not exist.
 */
fun componentId(serialName: String): ComponentId =
    componentId(geary.serializers.getClassFor(serialName))

/** Gets or registers the id of a component by its [kType]. */
fun componentId(kType: KType): ComponentId =
    componentId(kType.classifier as KClass<*>)

/** Gets or registers the id of a component by its [kClass]. */
fun componentId(kClass: KClass<*>): ComponentId =
    geary.componentProvider.getOrRegisterComponentIdForClass(kClass)


@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
@Suppress("UNUSED_PARAMETER")
fun componentId(kClass: KClass<out ComponentId>): Nothing =
    error("Trying to access id for component id")

/** Gets the [ComponentInfo] component from a component's id. */
fun ComponentId.getComponentInfo(): ComponentInfo? =
    this.toGeary().get()
//@ExperimentalAsyncGearyAPI
//public inline fun <T> runSafely(
//    scope: CoroutineScope = globalContext.engine,
//    crossinline run: suspend () -> T
//): Deferred<T> {
//    val deferred = globalContext.engine.async(start = CoroutineStart.LAZY) { run() }
//    globalContext.engine.runSafely(scope, deferred)
//    return deferred
//}
