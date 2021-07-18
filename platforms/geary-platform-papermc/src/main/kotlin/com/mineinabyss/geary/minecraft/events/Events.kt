package com.mineinabyss.geary.minecraft.events

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Stores a map of strings to actions which fire based on different game events.
 */
@Serializable(with = EventComponentSerializer::class)
@AutoscanComponent
public data class Events(
    override val wrapped: Map<String, List<GearyAction>>
) : FlatWrap<Map<String, List<GearyAction>>>

public object EventComponentSerializer : FlatSerializer<Events, Map<String, List<GearyAction>>>(
    "geary:events", serializer(), { Events(it) }
)
