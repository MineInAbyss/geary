package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Record

public interface EventRunner {
    public fun callEvent(target: Record, event: Record, source: Record?)
}
