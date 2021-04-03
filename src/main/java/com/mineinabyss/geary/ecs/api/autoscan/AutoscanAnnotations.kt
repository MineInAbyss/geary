package com.mineinabyss.geary.ecs.api.autoscan

import com.mineinabyss.geary.ecs.api.GearyComponent

/**
 * Excludes this class from having its serializer automatically registered for polymorphic serialization
 * with the Autoscanner.
 */
public annotation class ExcludeAutoscan

/**
 * Since [GearyComponent]s can be a subclass of [Any], we prefer to be explicit about autoscanning. Serializable
 * components must be marked with this annotation for the autoscanner to automatically register them.
 */
public annotation class AutoscanComponent
