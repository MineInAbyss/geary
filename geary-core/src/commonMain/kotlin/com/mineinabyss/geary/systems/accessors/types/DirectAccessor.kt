package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.ResultScope

class DirectAccessor<T>(val value: T) : Accessor<T> {
    override fun access(scope: ResultScope): T = value
}
