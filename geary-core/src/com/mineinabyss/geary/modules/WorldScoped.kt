package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.observers.builders.ObserverWithData
import com.mineinabyss.geary.observers.builders.ObserverWithoutData
import com.mineinabyss.geary.systems.builders.SystemBuilder
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query
import org.koin.core.component.get
import kotlin.reflect.KClass

@GearyDSL
interface WorldScoped : AutoCloseable {
    val closeables: MutableList<AutoCloseable>
    val world: Geary
    val logger get() = world.logger

    /**
     * Adds an [AutoCloseable] resource that will be closed right before the addon's onClose method is called.
     */
    fun <T : AutoCloseable> addCloseable(closeable: T): T {
        closeables += closeable
        return closeable
    }

    /**
     * Adds multiple [AutoCloseable] resources that will be closed right before the addon's onClose method is called.
     */
    fun addCloseables(vararg closeables: AutoCloseable) {
        this.closeables += closeables
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

    override fun close() {
        closeables.reversed().forEach { it.close() }
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
