package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.extend
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

public interface PrefabManagerContext : KoinComponent {
    public val prefabManager: PrefabManager
}

public val GearyContext.prefabManager: PrefabManager by extend { get() }
