package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent

public class PersistingComponents(
        public val persisting: MutableSet<GearyComponent> = mutableSetOf()
) : GearyComponent, MutableSet<GearyComponent> by persisting
