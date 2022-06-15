package com.mineinabyss.geary.components.events

/**
 * When a component is added to an entity, an event fires with this class as the value in a relation.
 *
 * The relation key describes the added component and allows event handlers to run when a specific component
 * gets added or replaced.
 *
 * For example, if listening to `A`, `B`, `C`, the event handler runs when one of the components is added,
 * as long as all three are present after the addition.
 */
public sealed class AddedComponent

public sealed class SetComponent
