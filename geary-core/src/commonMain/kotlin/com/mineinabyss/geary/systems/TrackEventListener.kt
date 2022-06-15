package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.events.GearyHandler

internal expect fun trackEventListener(
    listener: GearyListener,
    sourceListeners: MutableList<GearyListener>,
    targetListeners: MutableList<GearyListener>,
    archetypes: Family2ObjectArrayMap<Archetype>,
    eventHandlers: MutableList<GearyHandler>,
)
