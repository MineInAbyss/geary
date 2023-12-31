package com.mineinabyss.geary.datatypes

import kotlinx.serialization.Polymorphic

typealias GearyEntityType = EntityType
typealias GearyRecord = Record
typealias GearyRecords = Records
typealias GearyRelation = Relation

/**
 * GearyComponents aren't an interface because we like the option to be able to add `Any` class as a component to an
 * entity. However, for clarity we create this typealias.
 *
 * Since [Any] is not inherently serializable like an interface, when expecting a [Component] to be serializable,
 * use the [Polymorphic] annotation.
 */
typealias Component = Any

/** Type alias for entity IDs. */
typealias EntityId = ULong

/** Type alias for component IDs. Is the same as [EntityId]. */
typealias ComponentId = EntityId

typealias GearyComponent = Component
typealias GearyEntityId = EntityId
typealias GearyComponentId = ComponentId
