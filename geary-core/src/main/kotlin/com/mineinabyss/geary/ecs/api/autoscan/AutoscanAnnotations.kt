package com.mineinabyss.geary.ecs.api.autoscan

/**
 * Excludes this class from having its serializer automatically registered for polymorphic serialization
 * with the Autoscanner.
 */
public annotation class ExcludeAutoScan

/**
 * Indicates this system or event listener should be registered automatically by the AutoScanner.
 */
public annotation class AutoScan
