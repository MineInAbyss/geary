package com.mineinabyss.geary.datatypes

import kotlinx.serialization.Polymorphic

public typealias GearyEntity = Entity
public typealias GearyEntityType = EntityType
public typealias GearyRecord = Record
public typealias GearyRelation = Relation

/**
 * GearyComponents aren't an interface because we like the option to be able to add `Any` class as a component to an
 * entity. However, for clarity we create this typealias.
 *
 * Since [Any] is not inherently serializable like an interface, when expecting a [Component] to be serializable,
 * use the [Polymorphic] annotation.
 */
public typealias Component = Any

/** Type alias for entity IDs. */
public typealias EntityId = ULong

/** Type alias for component IDs. Is the same as [EntityId]. */
public typealias ComponentId = EntityId

public typealias GearyComponent = Component
public typealias GearyEntityId = EntityId
public typealias GearyComponentId = ComponentId
