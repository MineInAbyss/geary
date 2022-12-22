package com.mineinabyss.geary.prefabs

import okio.Path

class PrefabPath(
    val namespace: String,
    val get: () -> Sequence<Path>,
)
