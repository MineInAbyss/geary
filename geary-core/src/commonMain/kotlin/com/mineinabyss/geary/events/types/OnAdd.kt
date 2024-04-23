package com.mineinabyss.geary.events.types

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.GearyEntity

class OnAdd(val id: ComponentId)

class OnSet(val id: ComponentId)

class OnRemove(val id: ComponentId)

class OnUpdate(val id: ComponentId)

class OnExtend(val baseEntity: GearyEntity)
