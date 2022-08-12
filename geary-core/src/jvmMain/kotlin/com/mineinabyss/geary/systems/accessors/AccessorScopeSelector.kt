package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.types.DirectAccessor
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.typeOf

public actual interface AccessorScopeSelector {
    /** Automatically finds which [ResultScope] to select based on the receiver used on this [property]. */
    public operator fun <T : Accessor<*>> AccessorBuilder<T>.provideDelegate(
        thisRef: Listener,
        property: KProperty<*>
    ): T {
        val holder = property.getHolder(thisRef)
        return holder.addAccessor { build(holder, it) }
    }

    /** Ensures the [ResultScope] at the receiver of this [property] matches this family. */
    public operator fun Family.provideDelegate(thisRef: Listener, property: KProperty<*>): Accessor<Family> {
        val holder = property.getHolder(thisRef)
        holder._family.add(this)
        return holder.addAccessor {
            DirectAccessor(this)
        }
    }

    public companion object {
        private fun KProperty<*>.getHolder(thisRef: Listener) = when (extensionReceiverParameter?.type) {
            typeOf<SourceScope>() -> thisRef.source
            typeOf<TargetScope>() -> thisRef.target
            typeOf<EventScope>() -> thisRef.event
            else -> error("Can only define accessors for source, target, or event.")
        }
    }
}
