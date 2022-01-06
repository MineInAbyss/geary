package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.withRole
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

public fun interface AccessorBuilder<T : Accessor<*>> {
    public fun build(holder: AccessorHolder, index: Int): T
}

public interface AccessorBuilderProvider

public inline fun <reified T : GearyComponent?> AccessorBuilderProvider.getOrDefault(
    default: T
): AccessorBuilder<ComponentOrDefaultAccessor<T>> {
    val component = componentId<T>().withRole(HOLDS_DATA)
    return AccessorBuilder { holder, index ->
        ComponentOrDefaultAccessor(index, component, default)
    }
}

public inline fun <reified T : GearyComponent?> AccessorBuilderProvider.getOrNull(): AccessorBuilder<ComponentOrDefaultAccessor<T?>> {
    return getOrDefault(null)
}

public inline fun <reified T : GearyComponent> AccessorBuilderProvider.get(): AccessorBuilder<ComponentAccessor<T>> {
    val component = componentId<T>().withRole(HOLDS_DATA)
    return AccessorBuilder { holder, index ->
        holder.has(component)
        ComponentAccessor(index, component)
    }
}

public fun <T, R, A: Accessor<T>> AccessorBuilder<A>.map(
    transform: (T) -> R
): AccessorBuilder<Accessor<R>> = AccessorBuilder { holder, index ->
    MapAccessor(transform, this.build(holder, index))
}

public fun <T, A: Accessor<T>> AccessorBuilder<A>.together(): AccessorBuilder<ListAccessor<T, A>> =
    AccessorBuilder { holder, index ->
        ListAccessor(this.build(holder, index))
    }

/**
 * #### Nullability
 * If [K] or [V] are nullable, does not require that data be present on the respective part of the relation.
 *
 * Ex. `relation<String, Int?>()` will match the specific relation with key [String] and value [Int], regardless
 * of whether the entity has a component [Int] with data.
 *
 * #### Any
 * If one type is [Any], will access any relation matching the other type.
 *
 * Ex. `relation<String, Any>()` will match against any relation with a [String] key, that has a component
 * with data.
 */
public inline fun <reified K : GearyComponent?, reified V : GearyComponent> AccessorBuilderProvider.relation(): AccessorBuilder<RelationWithDataAccessor<K, V>> {
    return AccessorBuilder { holder, index ->
        val keyIsNullable = typeOf<K>().isMarkedNullable
        val anyKey = typeOf<K>() == typeOf<Any>()
        val anyValue = typeOf<V>() == typeOf<Any>()
        val relationKey = if (anyKey) null else componentId<K>()
        val relationValue = if (anyValue) null else RelationValueId(componentId<V>())

        when {
            relationKey != null && relationValue != null -> holder.has(Relation.of(relationKey, relationValue))
            relationValue != null -> holder.has(relationValue, componentMustHoldData = !keyIsNullable)
            //TODO need to add has check for key
        }

        RelationWithDataAccessor(index, keyIsNullable, relationValue, relationKey)
    }
}

public inline fun <T> AccessorBuilderProvider.added(): AccessorBuilder<Accessor<T>> {
    TODO()
}

public inline fun AccessorBuilderProvider.allAdded(): AccessorBuilder<Accessor<*>> {
    TODO()
}

public inline fun AccessorBuilderProvider.allAdded(vararg types: KClass<*>): AccessorBuilder<Accessor<Any>> {
    TODO()
}

public inline fun <T> AccessorBuilderProvider.anyAdded(): AccessorBuilder<Accessor<*>> {
    TODO()
}

public inline fun AccessorBuilderProvider.anyAdded(vararg types: KClass<*>): AccessorBuilder<Accessor<Any>> {
    TODO()
}

public class MapAccessor<T, R>(
    private val transform: (T) -> R,
    private val other: Accessor<T>
) : Accessor<R>(other.index) {
    init {
        _cached.addAll(other.cached)
    }

    override fun RawAccessorDataScope.readData(): List<R> {
        return other.run { readData() }.map(transform)
    }
}
