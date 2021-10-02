package com.mineinabyss.geary.ecs.components

import java.io.File

/**
 * A component applied to prefabs loaded from a file that allows them to be reused.
 */
public class Prefab(
    public val file: File
)
