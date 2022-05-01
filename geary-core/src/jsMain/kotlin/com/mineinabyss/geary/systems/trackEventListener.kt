package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.maps.Component2ObjectArrayMap
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.events.GearyHandler

internal actual fun trackEventListener(
    listener: GearyListener,
    sourceListeners: MutableList<GearyListener>,
    targetListeners: MutableList<GearyListener>,
    archetypes: Component2ObjectArrayMap<Archetype>,
    eventHandlers: MutableList<GearyHandler>
) {
    TODO("Currently unsupported due to lack of full reflection on js")
}