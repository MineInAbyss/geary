package com.mineinabyss.geary.prefabs

import kotlinx.io.Source
import kotlinx.io.files.Path

data class PrefabPath(
    val namespace: String,
    val paths: () -> Sequence<Path> = { emptySequence() },
    val sources: () -> Sequence<PrefabSource> = { emptySequence() },
)

data class PrefabSource(
    val source: Source,
    val key: PrefabKey,
    val formatExt: String,
)
