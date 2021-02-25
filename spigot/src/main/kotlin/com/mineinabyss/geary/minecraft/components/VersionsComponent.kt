package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property versions A map of component names to their current version. If one is not present the version is assumed to
 * be 0 in order to decrease serialized size.
 */
//TODO actually implement this
@Serializable
@SerialName("geary:versions")
@AutoscanComponent
public class VersionsComponent(
    public val versions: Map<String, Int> = mapOf()
)
