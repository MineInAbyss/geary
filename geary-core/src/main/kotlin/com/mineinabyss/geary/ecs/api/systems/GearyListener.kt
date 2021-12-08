package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.EventResultScope

public typealias EventRunner <T> = EventResultScope.(T) -> Unit

public abstract class GearyListener : AccessorHolder(), GearySystem
