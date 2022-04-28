package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.ResultScope

public class DirectAccessor<T>(public val value: T) : Accessor<T> {
    override fun access(scope: ResultScope): T = value
}
