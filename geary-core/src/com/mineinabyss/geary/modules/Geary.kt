package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.mineinabyss.geary.addons.dsl.GearyAddon
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
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

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
interface Geary : KoinComponent, WorldScoped {
    override val logger: Logger get() = get<Logger>()

    fun newScope(): WorldScoped {
        return addCloseable(object : WorldScoped {
            override val closeables: MutableList<AutoCloseable> = mutableListOf()
            override val world: Geary = this@Geary
        })
    }

    // By default, we always get the latest instance of deps, the Impl class gets them once for user
    // access where the engine isn't expected to be reloaded (ex. like it might be in tests)
    val eventRunner: EventRunner get() = get()
    val read: EntityReadOperations get() = get()
    val infoReader: EntityInfoReader get() = get()
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

    fun <T : Any> getAddon(addon: GearyAddon<T>): T = addons.getAddon(addon)

    fun <T : Any> getAddonOrNull(addon: GearyAddon<T>?): T? = addon?.let { addons.getAddonOrNull(addon) }

    fun tick() {
        engine.tick()
    }

    fun configure(setup: GearySetup.() -> Unit): Geary {
        GearySetup(getKoin()).setup()
        return this
    }

    companion object : Logger(mutableLoggerConfigInit(listOf(platformLogWriter())), "Geary") {
        operator fun invoke(application: Koin, logger: Logger? = null): Geary = Impl(application, logger)
    }

    class Impl(
        koin: Koin,
        logger: Logger? = null,
    ) : Geary {
        private val _koin = koin
        override val world: Geary = this@Impl
        override val closeables: MutableList<AutoCloseable> = mutableListOf()
        override val logger: Logger = logger ?: super.logger
        override val eventRunner: EventRunner by inject()
        override val read: EntityReadOperations by inject()
        override val infoReader: EntityInfoReader by inject()
        override val write: EntityMutateOperations by inject()
        override val queryManager: QueryManager by inject()
        override val pipeline: Pipeline by inject()
        override val entityProvider: EntityProvider by inject()
        override val entityRemoveProvider: EntityRemove by inject()
        override val components: Components by inject()
        override val componentProvider: ComponentProvider by inject()
        override val records: ArrayTypeMap by inject()
        override val engine: GearyEngine by inject()
        override val addons: MutableAddons by inject()
        override fun getKoin(): Koin = _koin
    }


    fun stringify() = getKoin().toString().removePrefix("org.koin.core.KoinApplication")
}

inline fun <reified K : Component?, reified T : Component> Geary.relationOf(): Relation =
    Relation.of(componentIdWithNullable<K>(), componentId<T>())

inline fun <reified K : Component?> Geary.relationOf(target: Entity): Relation =
    Relation.of(componentIdWithNullable<K>(), target.id)

inline fun Geary.findEntities(init: MutableFamily.Selector.And.() -> Unit) =
    findEntities(family(init))

inline fun Geary.findEntities(query: Query) = findEntities(query.buildFamily())
