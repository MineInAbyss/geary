package com.mineinabyss.geary.autoscan

import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
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
