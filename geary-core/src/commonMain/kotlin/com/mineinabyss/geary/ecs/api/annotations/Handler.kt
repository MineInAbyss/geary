package com.mineinabyss.geary.ecs.api.annotations

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler

/**
 * Indicates a function within a [GearyListener] should be registered as a [GearyHandler]
 *
 * The function can read from different accessors by adding arguments [SourceScope], [TargetScope], [EventScope].
 * They may appear in any order, be omitted, or used as a receiver.
 *
 * If [SourceScope] is nullable or omitted, the handler will not be called when there is no source present on the event.
 *
 * Example:
 *
 * ```kotlin
 * @Handler
 * fun TargetScope.doSomething(source: SourceScope, event: EventScope) {
 *     // Within here, you may use accessors defined for all three.
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
public annotation class Handler

