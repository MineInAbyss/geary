package com.mineinabyss.geary.ecs

import kotlinx.serialization.Polymorphic

/**
 * The base interface for components in the Geary ECS. Subclasses may or may not be serializable.
 *
 * When expecting a [GearyComponent] to be serializable, use the [Polymorphic] annotation.
 */
public typealias GearyComponent = Any
