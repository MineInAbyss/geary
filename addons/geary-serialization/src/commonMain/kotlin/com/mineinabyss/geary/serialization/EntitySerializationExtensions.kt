package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.annotations.optin.DangerousComponentOperation
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.observers.events.OnAdd
import com.mineinabyss.geary.serialization.components.Persists
import kotlin.reflect.KClass


/**
 * Sets a persisting [component] on this entity, which will be serialized if possible.
 *
 * @param noEvent If true, will not fire an [OnAdd] event.
 */
inline fun <reified T : Component> Entity.setPersisting(
    component: T,
    kClass: KClass<out T> = T::class,
    noEvent: Boolean = false
): T {
    set(component, kClass, noEvent)
    setRelation(serializableComponents.persists, componentId(kClass), Persists(), noEvent)
    return component
}

/**
 * Sets a list of persisting [components] on this entity.
 *
 * @param noEvent If true, will not fire an [OnAdd] event.
 * @see setPersisting
 */
fun Entity.setAllPersisting(
    components: Collection<Component>,
    override: Boolean = true,
    noEvent: Boolean = false
) {
    components.forEach {
        if (override || !has(it::class)) setPersisting(it, it::class, noEvent)
    }
}

/** Gets a persisting component of type [T] or adds a [default] if no component was present. */
inline fun <reified T : Component> Entity.getOrSetPersisting(
    kClass: KClass<out T> = T::class,
    default: () -> T
): T = get(kClass) ?: default().also { setPersisting(it, kClass) }

/** Gets all persisting components on this entity. */
fun Entity.getAllPersisting(): Set<Component> =
    getRelationsWithData<Persists, Any>().mapTo(mutableSetOf()) { it.targetData }

/** Gets all non-persisting components on this entity. */
fun Entity.getAllNotPersisting(): Set<Component> =
    getAll() - getAllPersisting()

@DangerousComponentOperation
fun Entity.setPersisting(components: Collection<Component>): Collection<Component> =
    setPersisting(component = components)
