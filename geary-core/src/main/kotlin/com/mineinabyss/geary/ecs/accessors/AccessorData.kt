package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.ComponentClass
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.*
import kotlin.reflect.KClass

/**
 * Some raw data [Accessor]s process to eventually build up to a [ResultScope].
 *
 * Extends [ArchetypeCacheScope] because anything in this scope should have access to
 * properties cached per archetype.
 *
 * @see Accessor
 */
public class RawAccessorDataScope(
    archetype: Archetype,
    perArchetypeData: List<List<Any?>>,
    public val row: Int,
    public val entity: GearyEntity,
) : ArchetypeCacheScope(archetype, perArchetypeData)

/**
 * Scope provided for accessor caching by archetype.
 */
public open class ArchetypeCacheScope(
    public val archetype: Archetype,
    public val perArchetypeData: List<List<Any?>>,
)


/**
 * Stores data which is formatted
 */
public class ResultScope(
    public val entity: GearyEntity,
    internal val data: List<*>,
    engine: GearyEngine
) : GearyAccessorScope(engine)

@Suppress("NOTHING_TO_INLINE")
public open class GearyAccessorScope(public val engine: GearyEngine) {
    /** Remove this entity from the ECS. */
    public fun GearyEntity.removeEntity() {
        engine.removeEntity(id)
    }

    /** Sets a component that holds data for this entity */
    public inline fun <reified T : GearyComponent> GearyEntity.set(component: T, kClass: KClass<out T> = T::class): T {
        engine.setComponentFor(id, engine.componentId(kClass), component)
        return component
    }

    @Deprecated(
        "Likely unintentionally using list as a single component, use set<T: GearyComponent>() if this is intentional.",
        ReplaceWith("setAll()")
    )
    @Suppress("UNUSED_PARAMETER")
    public fun GearyEntity.set(components: Collection<GearyComponent>): Unit =
        error("Trying to set a collection with set method instead of setAll")

    /** Sets components that hold data for this entity */
    public fun GearyEntity.setAll(components: Collection<GearyComponent>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) set(it, it::class)
        }
    }

    public inline fun <reified D : GearyComponent, reified Key : GearyComponent> GearyEntity.getRelation(): D? =
        getRelation(D::class, Key::class)

    public inline fun <D : GearyComponent> GearyEntity.getRelation(
        data: KClass<D>,
        key: KClass<*>
    ): D? {
        @Suppress("UNCHECKED_CAST") // internally ensured to always be true
        return get(Relation.of(data, key).id) as? D
    }

    public inline fun <reified D : GearyComponent, reified Key : GearyComponent> GearyEntity.setRelation(
        data: D,
    ) {
        setRelation(D::class, Key::class, data)
    }

    public fun <D : GearyComponent> GearyEntity.setRelation(
        dataKClass: KClass<D>,
        keyKClass: KClass<*>,
        data: D
    ) {
        engine.setComponentFor(id, Relation.of(dataKClass, keyKClass).id, data)
    }

    public inline fun <reified T : GearyComponent, reified C : GearyComponent> GearyEntity.removeRelation(): Boolean =
        removeRelation(Relation.of<T, C>())

    public fun GearyEntity.removeRelation(relation: Relation): Boolean =
        remove(Relation.of(relation.data, relation.key).id)

    /** Adds a list of [component] to this entity */
    public inline fun GearyEntity.add(component: GearyComponentId) {
        engine.addComponentFor(id, component)
    }

    public inline fun <reified T : GearyComponent> GearyEntity.add() {
        add(engine.componentId<T>())
    }

    public inline fun GearyEntity.addAll(components: Collection<GearyComponentId>) {
        components.forEach { add(it) }
    }

    /**
     * Adds a persisting [component] to this entity, which will be serialized in some way if possible.
     *
     * Ex. for bukkit entities this is done through a PersistentDataContainer.
     */
    public inline fun <reified T : GearyComponent> GearyEntity.setPersisting(
        component: T,
        kClass: KClass<out T> = T::class
    ): T {
        set(component, kClass)
        setRelation(
            dataKClass = PersistingComponent::class,
            keyKClass = kClass,
            data = PersistingComponent(),
        )
        return component
    }

    @Deprecated("Likely unintentionally using list as a single component", ReplaceWith("setAllPersisting()"))
    public fun GearyEntity.setPersisting(components: Collection<GearyComponent>): Collection<GearyComponent> =
        setPersisting(component = components)

    public inline fun GearyEntity.setAllPersisting(components: Collection<GearyComponent>, override: Boolean = true) {
        components.forEach {
            if (override || !has(it::class)) setPersisting(it, it::class)
        }
    }

    /**
     * Removes a component of type [T] from this entity.
     *
     * @return Whether the component was present before removal.
     */
    public inline fun <reified T : GearyComponent> GearyEntity.remove(): Boolean =
        remove(engine.componentId<T>()) || remove(engine.componentId<T>() and ENTITY_MASK)

    public inline fun GearyEntity.remove(kClass: ComponentClass): Boolean =
        remove(engine.componentId(kClass))

    public inline fun GearyEntity.remove(component: GearyComponentId): Boolean =
        engine.removeComponentFor(id, component)

    public inline fun GearyEntity.removeAll(components: Collection<GearyComponentId>): Boolean =
        components.any { remove(it) }

    /** Clears the components on this entity. */
    public fun GearyEntity.clear() {
        engine.clearEntity(id)
    }

    /** Gets a component of type [T] on this entity. */
    public inline fun <reified T : GearyComponent> GearyEntity.get(kClass: KClass<out T> = T::class): T? =
        get(engine.componentId(kClass)) as? T

    /** Gets a [component] which holds data from this entity. Use [has] if the component is not to hold data. */
    public inline fun GearyEntity.get(component: GearyComponentId): GearyComponent? =
        engine.getComponentFor(id, component)

    /** Gets a component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> GearyEntity.getOrSet(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T =
        get(kClass) ?: default().also { set(it) }

    /** Gets a persisting component of type [T] or adds a [default] if no component was present. */
    public inline fun <reified T : GearyComponent> GearyEntity.getOrSetPersisting(
        kClass: KClass<out T> = T::class,
        default: () -> T
    ): T = get(kClass) ?: default().also { setPersisting(it, kClass) }

    /** Gets all the active components on this entity. */
    public inline fun GearyEntity.getComponents(): Set<GearyComponent> = engine.getComponentsFor(id)

    /** Removes all components related to a parent of type [T] on the entity. */
    public inline fun <reified T : GearyComponent> GearyEntity.removeRelations(): Set<GearyComponent> {
        val comps = engine.getRelationsFor(id, RelationDataType(engine.componentId<T>()))
        comps.forEach { (_, relation) ->
            removeRelation(relation)
        }
        return comps.mapTo(mutableSetOf()) { it.first }
    }

    /** Gets a list of components related to the component represented by [T]. */
    public inline fun <reified T : GearyComponent> GearyEntity.getComponentsRelatedTo(): Set<GearyComponent> =
        getComponentsRelatedTo(RelationDataType(engine.componentId<T>()))

    /** Gets a list of components related to the component represented by [relationDataType]. */
    public inline fun GearyEntity.getComponentsRelatedTo(relationDataType: RelationDataType): Set<GearyComponent> =
        engine.getRelationsFor(id, relationDataType).mapTo(mutableSetOf()) { it.first }

    /** Gets all the active persisting components on this entity. */
    public inline fun GearyEntity.getPersistingComponents(): Set<GearyComponent> =
        getComponentsRelatedTo(RelationDataType(engine.componentId<PersistingComponent>()))

    //TODO update javadoc
    /** Gets all the active non-persisting components on this entity. */
    public inline fun GearyEntity.getInstanceComponents(): Set<GearyComponent> =
        getComponents() - getPersistingComponents()

    public inline fun GearyEntity.instanceOf(entity: GearyEntity): Boolean =
        has(entity.id.withRole(INSTANCEOF))

    /** Checks whether this entity has a component of type [T], regardless of whether or not it holds data. */
    public inline fun <reified T : GearyComponent> GearyEntity.has(kClass: KClass<out T> = T::class): Boolean =
        has(engine.componentId(kClass))

    /** Checks whether this entity has a [component], regardless of whether or not it holds data. */
    public inline fun GearyEntity.has(component: GearyComponentId): Boolean =
        engine.hasComponentFor(id, component)

    /** Checks whether an entity has all of a list of [components].
     * @see has */
    public inline fun GearyEntity.hasAll(components: Collection<ComponentClass>): Boolean = components.all { has(it) }

    public inline fun <reified T : Any> GearyEntity.callEvent(eventData: T) {
        engine.getRecord(id)?.apply {
            archetype.callEvent(T::class, eventData, row)
        }
    }
}