package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.idofront.di.DI
import okio.FileSystem

val fileSystem by DI.observe<FileSystem>()


fun FileSystemAddon(fileSystem: FileSystem) = createAddon<FileSystem>("File System", { fileSystem })
