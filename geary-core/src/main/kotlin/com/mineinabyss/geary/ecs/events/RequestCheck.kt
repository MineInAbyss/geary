package com.mineinabyss.geary.ecs.events

//TODO cancelable events
/**
 * A component attached to an event to request handler to check whether it should be successful.
 */
public object RequestCheck

/**
 * A component that gets added to events that failed a check.
 *
 * @see RequestCheck
 */
public object FailedCheck
