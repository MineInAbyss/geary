package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.dsl.createAddon
import okio.FileSystem

val FileSystemAddon = createAddon<FileSystem>(
    "File System",
    { error("No FileSystem passed into addon, please use FileSystemAddon(<filesystem>)") }
)
