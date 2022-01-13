package com.mineinabyss.geary.ecs.api.autoscan

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler
import com.mineinabyss.geary.ecs.query.Query

/**
 * Excludes this class from having its serializer automatically registered for component serialization
 * with the AutoScanner.
 */
public annotation class ExcludeAutoScan

/**
 * Indicates this [GearySystem], such as [TickingSystem], [GearyListener], or [Query] be registered automatically
 * on startup by the AutoScanner.
 */
public annotation class AutoScan

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
