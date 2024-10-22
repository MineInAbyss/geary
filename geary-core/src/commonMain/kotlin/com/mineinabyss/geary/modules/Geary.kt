package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.mineinabyss.geary.addons.dsl.Addon
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.EntityRemove
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.geary.observers.builders.ObserverWithData
import com.mineinabyss.geary.observers.builders.ObserverWithoutData
import com.mineinabyss.geary.systems.builders.SystemBuilder
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

/**
 * Root class for users to access geary functionality.
 *
 * Anything exposed to the user should be accessible here without
 * having to call functions on classes in the module,
 * it simply acts as a container for all dependencies.
 *
 * Any functions that modify the state of the engine modify it right away,
 * they are not scheduled for load phases like [GearySetup] is.
 */
interface Geary : KoinComponent {
    abstract val application: KoinApplication
    open val logger: Logger get() = application.koin.get()
    override fun getKoin(): Koin = application.koin

    // By default, we always get the latest instance of deps, the Impl class gets them once for user
    // access where the engine isn't expected to be reloaded (ex. like it might be in tests)
    val eventRunner: EventRunner get() = get()
    val read: EntityReadOperations get() = get()
    val write: EntityMutateOperations get() = get()
    val queryManager: QueryManager get() = get()
    val pipeline: Pipeline get() = get()
    val entityProvider: EntityProvider get() = get()
    val entityRemoveProvider: EntityRemove get() = get()
    val components: Components get() = get()
    val componentProvider: ComponentProvider get() = get()
    val records: ArrayTypeMap get() = get()
    val engine: GearyEngine get() = get()
    val addons: MutableAddons get() = get()

    fun <T : Addon<*, Inst>, Inst> getAddon(addon: T): Inst =
        addons.getInstance(addon) ?: error("Instance for addon ${addon.name} not found")

    fun <T : Addon<*, Inst>, Inst> getAddonOrNull(addon: T?): Inst? = addon?.let { addons.getInstance(addon) }

    fun <T : Addon<Conf, *>, Conf> getConfiguration(addon: T): Conf = addons.getConfig(addon)

    // Queries

    fun findEntities(family: Family): EntityArray {
        return queryManager.getEntitiesMatching(family).toEntityArray(world = this)
    }

    fun <T : Query> cache(
        query: T,
    ): CachedQuery<T> {
        return queryManager.trackQuery(query)
    }

    fun <T : Query> cache(
        create: (Geary) -> T,
    ): CachedQuery<T> {
        return cache(create(this))
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

        return SystemBuilder(defaultName, query, pipeline)
    }


    fun relationOf(kind: KClass<*>, target: KClass<*>): Relation =
        Relation.of(componentId(kind), componentId(target))

    fun EntityType.getArchetype(): Archetype =
        get<ArchetypeProvider>().getArchetype(this)

    /** Gets the entity associated with this [EntityId], stripping it of any roles. */
    fun EntityId.toGeary(): Entity = Entity(this and ENTITY_MASK, this@Geary)

    /** Gets the entity associated with this [Long]. */
    fun Long.toGeary(): Entity = Entity(toULong() and ENTITY_MASK, this@Geary)

    val NO_ENTITY: Entity get() = 0L.toGeary()

    companion object : Logger(mutableLoggerConfigInit(listOf(platformLogWriter())), "Geary") {
        operator fun invoke(application: KoinApplication, logger: Logger? = null): Geary = Impl(application, logger)
    }

    class Impl(
        override val application: KoinApplication,
        logger: Logger? = null,
    ) : Geary {
        override val logger: Logger = logger ?: super.logger
        override val eventRunner: EventRunner by inject()
        override val read: EntityReadOperations by inject()
        override val write: EntityMutateOperations by inject()
        override val queryManager: QueryManager by inject()
        override val pipeline: Pipeline by inject()
        override val entityProvider: EntityProvider by inject()
        override val entityRemoveProvider: EntityRemove by inject()
        override val components: Components by inject()
        override val componentProvider: ComponentProvider by inject()
        override val records: ArrayTypeMap by inject()
        override val engine: GearyEngine by inject()
    }

    fun stringify() = application.toString().removePrefix("org.koin.core.KoinApplication")
}

inline fun <reified K : Component?, reified T : Component> Geary.relationOf(): Relation =
    Relation.of(componentIdWithNullable<K>(), componentId<T>())

inline fun <reified K : Component?> Geary.relationOf(target: Entity): Relation =
    Relation.of(componentIdWithNullable<K>(), target.id)

inline fun <reified T : Any> Geary.get() = application.koin.get<T>()

// TODO simple api for running queries in place without caching
inline fun <T : Query> execute(
    query: T,
    run: T.() -> Unit = {},
): CachedQuery<T> {
    TODO()
}

inline fun <reified T : Any> Geary.observe(): ObserverWithoutData {
    return ObserverWithoutData(listOf(componentId<T>()), world = this) {
        eventRunner.addObserver(it)
    }
}

inline fun <reified T : Any> Geary.observeWithData(): ObserverWithData<T> {
    return ObserverWithData(listOf(componentId<T>()), world = this) {
        eventRunner.addObserver(it)
    }
}

inline fun Geary.findEntities(init: MutableFamily.Selector.And.() -> Unit) =
    findEntities(family(init))
