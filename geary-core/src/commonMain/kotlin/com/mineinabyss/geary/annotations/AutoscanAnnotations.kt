package com.mineinabyss.geary.annotations

import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.geary.systems.TickingSystem

/**
 * Excludes this class from having its serializer automatically registered for component serialization
 * with the AutoScanner.
 */
public annotation class ExcludeAutoScan

/**
 * Indicates this [GearySystem], such as [TickingSystem], [GearyListener], or [GearyQuery] be registered automatically
 * on startup by the AutoScanner.
 */
public annotation class AutoScan
