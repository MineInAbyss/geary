package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.events.Handler

internal expect fun trackEventListener(
    listener: Listener,
    sourceListeners: MutableList<Listener>,
    targetListeners: MutableList<Listener>,
    archetypes: Family2ObjectArrayMap<Archetype>,
    eventHandlers: MutableList<Handler>,
)
