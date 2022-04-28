package com.mineinabyss.geary.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a display name that should be presented to users.
 */
@JvmInline
@Serializable
@SerialName("geary:name")
public value class EntityName(public val name: String)
