package com.mineinabyss.geary.ecs.accessors.building

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.types.ComponentAccessor
import com.mineinabyss.geary.ecs.accessors.types.ComponentOrDefaultAccessor
import com.mineinabyss.geary.ecs.accessors.types.RelationWithDataAccessor
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.withRole
import kotlin.reflect.typeOf

/**
 * An empty interface that limits [AccessorBuilder] helper functions only to classes that use [Accessor]s.
 */
public interface AccessorBuilderProvider

/** Gets a component, ensuring it is on the entity. */
context(AccessorBuilderProvider, EngineContext) public inline fun <reified T : GearyComponent> get(): AccessorBuilder<ComponentAccessor<T>> {
    return AccessorBuilder { holder, index ->
        val component = componentId<T>().withRole(HOLDS_DATA)
        holder.has(component)
        ComponentAccessor(index, component)
    }
}

/** Gets a component or provides a [default] if the entity doesn't have it. */
context(AccessorBuilderProvider, EngineContext) public inline fun <reified T : GearyComponent?> getOrDefault(
    default: T
): AccessorBuilder<ComponentOrDefaultAccessor<T>> {
    return AccessorBuilder { _, index ->
        val component = componentId<T>().withRole(HOLDS_DATA)
        ComponentOrDefaultAccessor(index, component, default)
    }
}

/** Gets a component or `null` if the entity doesn't have it. */
context(AccessorBuilderProvider, EngineContext) public inline fun <reified T : GearyComponent?> getOrNull(): AccessorBuilder<ComponentOrDefaultAccessor<T?>> {
    return com.mineinabyss.geary.ecs.accessors.building.getOrDefault(null)
}


/**
 * This function allows you to access a specific relation or all relations with a certain key or value.
 *
 * #### Nullability
 * If [K] is nullable, just the relation needs to be present.
 *
 * If [K] is NOT nullable, the entity must have a component of type [K] set.
 *
 * - `relation<String?, Int>()` will match the specific relation with key [String] and value [Int], regardless
 * of whether the entity has a component [Int] with data.
 *
 * #### Matching any relation
 * If one type is [Any], this will get all relations matching the other type.
 *
 * - `relation<String?, Any>()` will match against all relations with a [String] key and any value.
 * - `relation<Any, String>()` will match against all relations with any key and a [String] value,
 *   so long as the entity also has a key of that type set.
 *
 * @see flatten
 */
context(AccessorBuilderProvider, EngineContext) public inline fun <reified K : GearyComponent?, reified V : GearyComponent> relation(): AccessorBuilder<RelationWithDataAccessor<K, V>> {
    return AccessorBuilder { holder, index ->
        val key = typeOf<K>()
        val value = typeOf<V>()
        val keyIsNullable = key.isMarkedNullable
        val relationKey = if (key.classifier == Any::class) null else componentId(key)
        val relationValue = if (value.classifier == Any::class) null else RelationValueId(componentId(value))
        //TODO could we reuse code between hasRelation and here?
        holder.hasRelation(key, value)
        RelationWithDataAccessor(index, keyIsNullable, relationValue, relationKey)
    }
}

