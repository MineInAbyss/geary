package com.mineinabyss.geary.annotations

import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.events.GearyHandler

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

