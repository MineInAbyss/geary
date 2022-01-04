package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.typeOf

public typealias EventRunner <T> = EventScope.(T) -> Unit

public abstract class GearyListener : AccessorHolder(), GearySystem

public abstract class GearyInteraction : GearySystem, AccessorBuilderProvider {
    public val source: AccessorHolder = object : AccessorHolder() {}
    public val target: AccessorHolder = object : AccessorHolder() {}
    public val event: AccessorHolder = object : AccessorHolder() {}

    public operator fun <T : Accessor<*>> AccessorBuilder<T>.provideDelegate(
        thisRef: Any,
        property: KProperty<*>
    ): T {
        val holder = when (property.extensionReceiverParameter?.type) {
            typeOf<SourceScope>() -> source
            typeOf<TargetScope>() -> target
            typeOf<EventScope>() -> event
            else -> error("")
        }
        return holder.addAccessor { build(holder, it) }
    }
}

public annotation class Handler
