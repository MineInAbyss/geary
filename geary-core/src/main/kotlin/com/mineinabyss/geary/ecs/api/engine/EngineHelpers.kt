package com.mineinabyss.geary.ecs.api.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.components.ComponentInfo
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.reflect.KClass
import kotlin.reflect.KType

/** Creates a new empty entity. May reuse recently deleted entity ids. */
public fun EngineScope.entity(): GearyEntity = engine.newEntity()

/** @see entity */
public inline fun EngineScope.entity(run: GearyEntity.() -> Unit): GearyEntity = entity().apply(run)

/** Creates a new empty entity that will get removed once [run] completes or fails. */
public suspend inline fun <T> EngineScope.temporaryEntity(
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
public suspend inline fun <reified T> EngineScope.componentId(): GearyComponentId = componentId(T::class)

/**
 * Gets the id of a component by its serial name.
 * Throws an error if the component name does not exist.
 */
public suspend fun EngineScope.componentId(serialName: String): GearyComponentId =
    componentId(Formats.getClassFor(serialName))

/** Gets or registers the id of a component by its [kType]. */
public suspend fun EngineScope.componentId(kType: KType): GearyComponentId =
    componentId(kType.classifier as KClass<*>)

/** Gets or registers the id of a component by its [kClass]. */
public suspend fun EngineScope.componentId(kClass: KClass<*>): GearyComponentId =
    engine.getOrRegisterComponentIdForClass(kClass)

@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
@Suppress("UNUSED_PARAMETER")
public fun EngineScope.componentId(kClass: KClass<out GearyComponentId>): Nothing =
    error("Trying to access id for component id")

/** Gets the [ComponentInfo] component from a component's id. */
public suspend fun GearyComponentId.getComponentInfo(): ComponentInfo? = this.toGeary().get()

//@Deprecated("This will be replaced with multi-receiver access in Kotlin 1.6.20")
//public val globalEngine: Engine get() = KoinPlatformTools.defaultContext().get().get<Engine>()

public fun EngineScope.systems(vararg systems: GearySystem): List<Deferred<Unit>> {
    return systems.map { engine.async { engine.addSystem(it) } }
}

public suspend inline fun <T> Engine.withLock(entities: Collection<GearyEntity>, run: () -> T): T {
    // We need to have consistent sorting so there's never a situation where the next entity we need is locked by a
    // request that also needs one of the previous entities. If everyone is sorting the same way, this is impossible.
    entities.sortedBy { it.id }.forEach { lock(it) }
    return try {
        run()
    } finally {
        entities.forEach { unlock(it) }
    }
}
