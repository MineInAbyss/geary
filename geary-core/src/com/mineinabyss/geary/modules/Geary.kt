package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.mineinabyss.features.Feature
import com.mineinabyss.features.FeatureManager
import com.mineinabyss.features.get
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
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.direct
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
interface Geary : DI, WorldScoped {
    override val logger: Logger get() = direct.instance<Logger>()

    fun newScope(): WorldScoped {
        return addCloseable(object : WorldScoped {
            override val closeables: MutableList<AutoCloseable> = mutableListOf()
            override val world: Geary = this@Geary
        })
    }

    // By default, we always get the latest instance of deps, the Impl class gets them once for user
    // access where the engine isn't expected to be reloaded (ex. like it might be in tests)
    val eventRunner: EventRunner get() = direct.instance()
    val read: EntityReadOperations get() = direct.instance()
    val infoReader: EntityInfoReader get() = direct.instance()
    val write: EntityMutateOperations get() = direct.instance()
    val queryManager: QueryManager get() = direct.instance()
    val pipeline: Pipeline get() = direct.instance()
    val entityProvider: EntityProvider get() = direct.instance()
    val entityRemoveProvider: EntityRemove get() = direct.instance()
    val components: Components get() = direct.instance()
    val componentProvider: ComponentProvider get() = direct.instance()
    val records: ArrayTypeMap get() = direct.instance()
    val engine: GearyEngine get() = direct.instance()
    val addons: FeatureManager get() = direct.instance()

    fun <T : Any> getAddon(addon: Feature<T>): T = addons.get(addon)

    fun <T : Any> getAddonOrNull(addon: Feature<T>?): T? = addon?.let { addons.getOrNull(addon) }

    fun tick() {
        engine.tick()
    }

    fun configure(setup: GearySetup.() -> Unit): Geary {
        GearySetup(di).setup()
        return this
    }

    companion object : Logger(mutableLoggerConfigInit(listOf(platformLogWriter())), "Geary") {
        operator fun invoke(di: DI, logger: Logger? = null): Geary = Impl(di, logger)
    }

    class Impl(
        di: DI,
        logger: Logger? = null,
    ) : Geary, DirectDI by di.direct {
        override val world: Geary = this@Impl
        override val closeables: MutableList<AutoCloseable> = mutableListOf()
        override val logger: Logger = logger ?: super.logger
        override val eventRunner: EventRunner by di.instance()
        override val read: EntityReadOperations by di.instance()
        override val infoReader: EntityInfoReader by di.instance()
        override val write: EntityMutateOperations by di.instance()
        override val queryManager: QueryManager by di.instance()
        override val pipeline: Pipeline by di.instance()
        override val entityProvider: EntityProvider by di.instance()
        override val entityRemoveProvider: EntityRemove by di.instance()
        override val components: Components by di.instance()
        override val componentProvider: ComponentProvider by di.instance()
        override val records: ArrayTypeMap by di.instance()
        override val engine: GearyEngine by di.instance()
        override val addons: FeatureManager by di.instance()
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
