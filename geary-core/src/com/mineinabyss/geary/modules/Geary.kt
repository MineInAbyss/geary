package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.mineinabyss.features.Feature
import com.mineinabyss.features.FeatureManager
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.EntityRemove
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.geary.systems.query.Query
import org.kodein.di.DirectDI
import org.kodein.di.instance

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
interface Geary : WorldScoped {
    override val logger: Logger get() = instance<Logger>()

    fun newScope(di: DirectDI = directDI): WorldScoped {
        return addCloseable(object : WorldScoped {
            override val closeables: MutableList<AutoCloseable> = mutableListOf()
            override val world: Geary = this@Geary
            override val directDI: DirectDI = di
        })
    }

    // By default, we always get the latest instance of deps, the Impl class gets them once for user
    // access where the engine isn't expected to be reloaded (ex. like it might be in tests)
    val eventRunner: EventRunner get() = instance()
    val read: EntityReadOperations get() = instance()
    val infoReader: EntityInfoReader get() = instance()
    val write: EntityMutateOperations get() = instance()
    val queryManager: QueryManager get() = instance()
    val pipeline: Pipeline get() = instance()
    val entityProvider: EntityProvider get() = instance()
    val entityRemoveProvider: EntityRemove get() = instance()
    val components: Components get() = instance()
    val componentProvider: ComponentProvider get() = instance()
    val records: ArrayTypeMap get() = instance()
    val engine: GearyEngine get() = instance()
    val addons: FeatureManager get() = instance()

    fun <T : Any> getAddon(addon: Feature<T>): T = addons.get(addon)

    fun <T : Any> getAddonOrNull(addon: Feature<T>?): T? = addon?.let { addons.getOrNull(addon) }

    fun tick() {
        engine.tick()
    }

    fun configure(setup: GearySetup.() -> Unit): Geary {
        GearySetup(directDI).setup()
        return this
    }

    companion object : Logger(mutableLoggerConfigInit(listOf(platformLogWriter())), "Geary") {
        operator fun invoke(di: DirectDI, logger: Logger? = null): Geary = Impl(di, logger)
    }

    class Impl(
        di: DirectDI,
        logger: Logger? = null,
    ) : Geary {
        override val directDI: DirectDI = di
        override val world: Geary = this@Impl
        override val closeables: MutableList<AutoCloseable> = mutableListOf()
        override val logger: Logger = logger ?: super.logger
        override val eventRunner: EventRunner = instance()
        override val read: EntityReadOperations = instance()
        override val infoReader: EntityInfoReader = instance()
        override val write: EntityMutateOperations = instance()
        override val queryManager: QueryManager = instance()
        override val pipeline: Pipeline = instance()
        override val entityProvider: EntityProvider = instance()
        override val entityRemoveProvider: EntityRemove = instance()
        override val components: Components = instance()
        override val componentProvider: ComponentProvider = instance()
        override val records: ArrayTypeMap = instance()
        override val engine: GearyEngine = instance()
        override val addons: FeatureManager = instance()
    }

    fun stringify() = instance<String>("name")
}

inline fun <reified K, reified T : Component> Geary.relationOf(): Relation =
    Relation.of(componentIdWithNullable<K>(), componentId<T>())

inline fun <reified K> Geary.relationOf(target: Entity): Relation =
    Relation.of(componentIdWithNullable<K>(), target.id)

inline fun Geary.findEntities(init: MutableFamily.Selector.And.() -> Unit) =
    findEntities(family(init))

inline fun Geary.findEntities(query: Query) = findEntities(query.buildFamily())
