package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler

internal actual fun trackEventListener(
    listener: GearyListener,
    sourceListeners: MutableList<GearyListener>,
    targetListeners: MutableList<GearyListener>,
    archetypes: Component2ObjectArrayMap<Archetype>,
    eventHandlers: MutableList<GearyHandler>
) {
    TODO("Currently unsupported due to lack of full reflection on js")
}
