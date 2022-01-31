package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.*
import com.mineinabyss.geary.ecs.accessors.building.AccessorBuilder
import com.mineinabyss.geary.ecs.accessors.building.AccessorBuilderProvider
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.accessors.types.ComponentAccessor
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.withRole
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.typeOf

/**
 * #### [Guide: Listeners](https://wiki.mineinabyss.com/geary/guide/listeners)
 *
 * Exposes a way to match against certain combinations of [source]/[target]/[event] entities present on a fired event.
 *
 * [GearyHandler]s can be defined inside by annotating a function with [Handler], these
 * are the actual functions that run when a matching event is found.
 */
public abstract class GearyListener : GearySystem, AccessorBuilderProvider {
    override val engine: Engine by inject()

    public val source: AccessorHolder = AccessorHolder()
    public val target: AccessorHolder = AccessorHolder()
    public val event: AccessorHolder = AccessorHolder()

    public operator fun <T : Accessor<*>> AccessorBuilder<T>.provideDelegate(
        thisRef: Any,
        property: KProperty<*>
    ): T {
        val holder = when (property.extensionReceiverParameter?.type) {
            typeOf<SourceScope>() -> source
            typeOf<TargetScope>() -> target
            typeOf<EventScope>() -> event
            else -> error("Can only define accessors for source, target, or event.")
        }
        return holder.addAccessor { runBlocking { build(holder, it) } }
    }

    public suspend fun start() {
        onStart()
        // Build these after so subclasses can modify source/target/event in onStart
        source.start()
        target.start()
        event.start()
    }
    protected open suspend fun onStart() {}

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: SourceScope, property: KProperty<*>): T =
        thisRef.data[index] as T

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: TargetScope, property: KProperty<*>): T =
        thisRef.data[index] as T

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: EventScope, property: KProperty<*>): T =
        thisRef.data[index] as T

    /** Gets a component, ensuring it is on the entity. */
    public inline fun <reified T : GearyComponent> added(): AccessorBuilder<ComponentAccessor<T>> {
        return AccessorBuilder { holder, index ->
            event.onAdded(componentId<T>().withRole(HOLDS_DATA))
            get<T>().build(holder, index)
        }
    }

    //TODO allow checking that all components were added on source
    //TODO an Accessor which returns the specific component added.
    public fun AccessorHolder.allAdded() {
        family.components.forEach {
            event.onAdded(it)
        }
    }
}

