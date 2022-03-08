package com.mineinabyss.geary.ecs.api

import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.systems.QueryContext
import com.mineinabyss.geary.ecs.serialization.Formats

public interface GearyContext :
    EngineContext,
    FormatsContext,
    QueryContext

public interface FormatsContext {
    public val formats: Formats
}
