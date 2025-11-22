package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.modules.WorldScoped
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** Creates a new empty entity. May reuse recently deleted entity ids. */
fun WorldScoped.entity(): Entity = Entity(world.entityProvider.create(), world)

/** @see entity */
inline fun WorldScoped.entity(run: Entity.() -> Unit): Entity = entity().apply(run)

/** Creates a new empty entity that will get removed once [run] completes or fails. */
inline fun <T> WorldScoped.temporaryEntity(
    run: (Entity) -> T,
): T {
    val entity = entity {
        add(world.components.suppressRemoveEvent, noEvent = true)
    }
    return try {
        run(entity)
    } finally {
        entity.removeEntity()
    }
}

inline fun <reified T> WorldScoped.component(): Entity = component(T::class)

fun WorldScoped.component(kClass: KClass<*>): Entity = componentId(kClass).toGeary()

/** Gets or registers the id of a component of type [T] */
inline fun <reified T> WorldScoped.componentId(): ComponentId = componentId(T::class)

/** Gets or registers the id of a component of type [T], adding the [HOLDS_DATA] role if [T] is not nullable. */
inline fun <reified T> WorldScoped.componentIdWithNullable(): ComponentId =
    componentId<T>().withRole(if (typeOf<T>().isMarkedNullable) NO_ROLE else HOLDS_DATA)

/** Gets or registers the id of a component by its [kType]. */
fun WorldScoped.componentId(kType: KType): ComponentId =
    componentId(kType.classifier ?: error("No classifier found for type $kType"))

/** Gets or registers the id of a component by its [kClass]. */
fun WorldScoped.componentId(kClass: KClassifier): ComponentId =
    world.componentProvider.getOrRegisterComponentIdForClass(kClass)


@Deprecated("Should not be getting an id for an id!", ReplaceWith("componentId(component)"))
@Suppress("UNUSED_PARAMETER")
fun componentId(kClass: KClass<out ComponentId>): Nothing =
    error("Trying to access id for component id")

/** Gets the [ComponentInfo] component from a component's id. */
fun WorldScoped.getComponentInfo(component: ComponentId): ComponentInfo? = component.toGeary().get<ComponentInfo>()

inline fun <reified T> WorldScoped.cId(): ComponentId = componentId<T>()
