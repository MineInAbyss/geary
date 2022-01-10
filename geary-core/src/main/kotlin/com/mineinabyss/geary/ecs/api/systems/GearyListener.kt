package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.*
import com.mineinabyss.geary.ecs.events.AddedComponent
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.typeOf

public abstract class GearyListener : GearySystem, AccessorBuilderProvider {
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
            else -> error("")
        }
        return holder.addAccessor { build(holder, it) }
    }

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: SourceScope, property: KProperty<*>): T =
        thisRef.data[index] as T

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: TargetScope, property: KProperty<*>): T =
        thisRef.data[index] as T

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> Accessor<T>.getValue(thisRef: EventScope, property: KProperty<*>): T =
        thisRef.data[index] as T

    public fun allAdded()/*: AccessorBuilder<Accessor<*>>*/ {
        event.or {
            target.family.components.forEach { hasRelation(it, typeOf<AddedComponent>()) }
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
public annotation class Handler
