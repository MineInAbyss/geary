package com.mineinabyss.geary.modules

import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.observers.builders.ObserverWithData
import com.mineinabyss.geary.observers.builders.ObserverWithoutData
import com.mineinabyss.geary.systems.builders.SystemBuilder
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KClass

interface MutableWorldScoped : WorldScoped, MutableDI {

}

interface WorldScoped : DI {
    val world: Geary
    val logger get() = world.logger


    /**
     * Creates a child [WorldScoped] context for this world which can register queries, systems, and observers.
     *
     * Closing the scope will unregister anything registered in it, except any entities it created.
     * Reading from closed queries throws an error.
     *
     * Ensures the scope closes when the world closes.
     */
    fun newScope(): WorldScoped {
        return object : WorldScoped {
            override val world: Geary = this@WorldScoped.world
            override val di: DIContext = this@WorldScoped.di.subcontext().di
        }
    }

    fun <T : Query> cache(
        query: T,
    ): CachedQuery<T> {
        return addCloseable(world.queryManager.trackQuery(query))
    }

    fun <T : Query> cache(
        create: (Geary) -> T,
    ): CachedQuery<T> {
        return addCloseable(cache(create(world)))
    }

    fun <T : Query> system(
        query: T,
    ): SystemBuilder<T> {
        val defaultName = Throwable().stackTraceToString()
            .lineSequence()
            .drop(2) // First line error, second line is this function
            .first()
            .trim()
            .substringBeforeLast("(")
            .substringAfter("$")
            .substringAfter("Kt.")
            .substringAfter("create")

        return SystemBuilder(this, world.pipeline, defaultName, query)
    }


    // Queries

    fun findEntities(family: Family): EntityArray {
        return world.queryManager.getEntitiesMatching(family).toEntityArray(world = world)
    }

    fun relationOf(kind: KClass<*>, target: KClass<*>): Relation =
        Relation.of(componentId(kind), componentId(target))

    fun EntityType.getArchetype(): Archetype = world.get<ArchetypeProvider>().getArchetype(this)

    /** Gets the entity associated with this [EntityId], stripping it of any roles. */
    fun EntityId.toGeary(): Entity = Entity(this and ENTITY_MASK, world)

    /** Gets the entity associated with this [Long]. */
    fun Long.toGeary(): Entity = Entity(toULong() and ENTITY_MASK, world)

    val NO_ENTITY: Entity get() = 0L.toGeary()

    fun <T : Any> getAddon(addon: DI.ModuleWithConfig<T>): T =
        world.scope.getOrNull(addon) ?: error("Addon not loaded ${addon.name}")

    fun <T : Any> getAddonOrNull(addon: DI.ModuleWithConfig<T>?): T? = addon?.let { world.scope[addon] }

    fun tick() {
        world.engine.tick()
    }

    fun <T> use(block: WorldScoped.() -> T): T {
        return (this as AutoCloseable).use {
            block()
        }
    }
}

inline fun <reified T : Any> WorldScoped.observe(name: String? = null): ObserverWithoutData {
    return ObserverWithoutData(
        listOf(world.componentId<T>()),
        world = world,
        onBuild = {
            world.eventRunner.addObserver(it)
            addCloseable(it)
        },
        onClose = { world.eventRunner.removeObserver(it) }
    )
}

inline fun <reified T : Any> WorldScoped.observeWithData(name: String? = null): ObserverWithData<T> {
    return ObserverWithData(
        listOf(world.componentId<T>()),
        world = world,
        onBuild = {
            world.eventRunner.addObserver(it)
            addCloseable(it)
        },
        onClose = { world.eventRunner.removeObserver(it) }
    )
}

inline fun <reified K, reified T : Component> WorldScoped.relationOf(): Relation =
    Relation.of(componentIdWithNullable<K>(), componentId<T>())

inline fun <reified K> WorldScoped.relationOf(target: Entity): Relation =
    Relation.of(componentIdWithNullable<K>(), target.id)

inline fun WorldScoped.findEntities(init: MutableFamily.Selector.And.() -> Unit) =
    findEntities(family(init))

inline fun WorldScoped.findEntities(query: Query) = findEntities(query.buildFamily())
