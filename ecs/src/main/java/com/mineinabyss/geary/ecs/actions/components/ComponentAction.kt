package com.mineinabyss.geary.ecs.actions.components

import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.serialization.Formats
import kotlinx.serialization.Serializable

//TODO figure out something better, especially don't like having an extension function on strings for this
/**
 * An abstract class for [GearyAction]s that require a list of component types to act upon.
 *
 * @property components The serial names of components for this action to act upon.
 * @property componentClasses Lazily evaluated classes associated with the [component serial names][components].
 */
@Serializable
public abstract class ComponentAction : GearyAction() {
    internal abstract val components: Set<String>

    internal val componentClasses by lazy { components.map { it.toComponentClass() } }
}

internal fun String.toComponentClass() = Formats.componentSerialNames[this] ?: error("$this is not a valid component name")
