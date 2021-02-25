package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable(with = PassiveActionsSerializer::class)
@AutoscanComponent
public data class PassiveActionsComponent(
    override val wrapped: List<GearyAction>
) : FlatWrap<List<GearyAction>>

public object PassiveActionsSerializer : FlatSerializer<PassiveActionsComponent, List<GearyAction>>(
    "geary:passive", serializer(), { PassiveActionsComponent(it) }
)
