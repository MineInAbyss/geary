package com.mineinabyss.geary.autoscan

import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.geary.systems.query.GearyQuery

/**
 * Excludes this class from having its serializer automatically registered for component serialization
 * with the AutoScanner.
 */
annotation class ExcludeAutoScan

/**
 * Indicates this [GearySystem], such as [RepeatingSystem], [Observer], or [GearyQuery] be registered automatically
 * on startup by the AutoScanner.
 */
annotation class AutoScan
