package com.mineinabyss.geary.prefabs

import org.koin.core.component.KoinComponent
import org.koin.core.component.get

public interface PrefabManagerScope : KoinComponent {
    public val prefabManager: PrefabManager

    public companion object {
        public inline fun <T> run(run: PrefabManagerScope.() -> T): T = object : PrefabManagerScope {
            override val prefabManager: PrefabManager = get()
        }.run(run)
    }
}
