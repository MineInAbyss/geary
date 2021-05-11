package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * > geary:passive
 *
 * A component that stores a list of actions that should be run passively at a certain interval.
 */
@Serializable(with = PassiveActionsSerializer::class)
@AutoscanComponent
public data class PassiveActionsComponent(
    override val wrapped: List<GearyAction>
) : FlatWrap<List<GearyAction>>

public object PassiveActionsSerializer : FlatSerializer<PassiveActionsComponent, List<GearyAction>>(
    "geary:passive", serializer(), { PassiveActionsComponent(it) }
)
