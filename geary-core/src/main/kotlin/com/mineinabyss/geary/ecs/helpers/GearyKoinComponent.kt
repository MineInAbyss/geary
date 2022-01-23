package com.mineinabyss.geary.ecs.helpers

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public open class GearyKoinComponent: KoinComponent {
    public val engine: Engine by inject()
    public val queryManager: QueryManager by inject()
}
