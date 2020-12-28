package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.Serializable

@Serializable
public abstract class ComponentAction : GearyAction() {
    internal abstract val components: Set<String>

    internal val componentClasses by lazy { components.map { it.toComponentClass() } }
}

internal fun String.toComponentClass() = Formats.componentSerialNames[this] ?: error("$this is not a valid component name")
