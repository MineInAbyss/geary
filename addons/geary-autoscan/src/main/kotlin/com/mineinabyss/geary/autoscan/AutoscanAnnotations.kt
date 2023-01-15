package com.mineinabyss.geary.autoscan

import com.mineinabyss.geary.systems.GearySystem
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.query.GearyQuery

/**
 * Excludes this class from having its serializer automatically registered for component serialization
 * with the AutoScanner.
 */
annotation class ExcludeAutoScan

/**
 * Indicates this [GearySystem], such as [RepeatingSystem], [Listener], or [GearyQuery] be registered automatically
 * on startup by the AutoScanner.
 */
annotation class AutoScan
