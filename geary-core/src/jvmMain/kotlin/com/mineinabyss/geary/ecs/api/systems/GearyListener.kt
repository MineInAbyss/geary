package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.AccessorBuilder
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.typeOf


public operator fun <T : Accessor<*>> AccessorBuilder<T>.provideDelegate(
    thisRef: GearyListener,
    property: KProperty<*>
): T {
    val holder = when (property.extensionReceiverParameter?.type) {
        typeOf<SourceScope>() -> thisRef.source
        typeOf<TargetScope>() -> thisRef.target
        typeOf<EventScope>() -> thisRef.event
        else -> error("Can only define accessors for source, target, or event.")
    }
    return holder.addAccessor { build(holder, it) }
}
