package com.mineinabyss.geary.prefabs

import org.koin.core.component.KoinComponent
import org.koin.core.component.get

public interface PrefabManagerContext : KoinComponent {
    public val prefabManager: PrefabManager

    public companion object {
        public inline fun <T> run(run: PrefabManagerContext.() -> T): T = object : PrefabManagerContext {
            override val prefabManager: PrefabManager = get()
        }.run(run)
    }
}
