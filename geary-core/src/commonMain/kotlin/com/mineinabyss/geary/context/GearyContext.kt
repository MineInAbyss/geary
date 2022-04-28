package com.mineinabyss.geary.context

import com.mineinabyss.geary.systems.QueryContext

public interface GearyContext :
    EngineContext,
    FormatsContext,
    SerializersContext,
    QueryContext
