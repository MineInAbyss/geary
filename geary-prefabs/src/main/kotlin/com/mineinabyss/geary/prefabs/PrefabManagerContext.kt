package com.mineinabyss.geary.prefabs

import org.koin.core.component.KoinComponent

public interface PrefabManagerContext : KoinComponent {
    public val prefabManager: PrefabManager
}
