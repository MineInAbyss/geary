package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.modules.Geary
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** Creates a new empty entity. May reuse recently deleted entity ids. */
fun Geary.entity(): Entity = Entity(module.entityProvider.create(), this)

/** @see entity */
inline fun Geary.entity(run: Entity.() -> Unit): Entity = entity().apply(run)

/** Creates a new empty entity that will get removed once [run] completes or fails. */
inline fun <T> Geary.temporaryEntity(
    run: (Entity) -> T,
): T {
    val entity = entity {
        add(module.components.suppressRemoveEvent, noEvent = true)
    }
    return try {
        run(entity)
    } finally {
        entity.removeEntity()
    }
}

inline fun <reified T> Geary.component(): Entity = component(T::class)

fun Geary.component(kClass: KClass<*>): Entity = componentId(kClass).toGeary(this)

/** Gets or registers the id of a component of type [T] */
inline fun <reified T> Geary.componentId(): ComponentId = componentId(T::class)

/** Gets or registers the id of a component of type [T], adding the [HOLDS_DATA] role if [T] is not nullable. */
inline fun <reified T> Geary.componentIdWithNullable(): ComponentId =
    componentId<T>().withRole(if (typeOf<T>().isMarkedNullable) NO_ROLE else HOLDS_DATA)

/** Gets or registers the id of a component by its [kType]. */
fun Geary.componentId(kType: KType): ComponentId =
    componentId(kType.classifier ?: error("No classifier found for type $kType"))

/** Gets or registers the id of a component by its [kClass]. */
fun Geary.componentId(kClass: KClassifier): ComponentId =
    module.componentProvider.getOrRegisterComponentIdForClass(kClass)


@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
@Suppress("UNUSED_PARAMETER")
fun componentId(kClass: KClass<out ComponentId>): Nothing =
    error("Trying to access id for component id")

/** Gets the [ComponentInfo] component from a component's id. */
fun ComponentId.getComponentInfo(world: Geary): ComponentInfo? = this.toGeary(world).get()

inline fun <reified T> Geary.cId(): ComponentId = componentId<T>()
