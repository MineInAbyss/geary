package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.idofront.di.DI
import okio.FileSystem

val fileSystem by DI.observe<FileSystem>()

interface FileSystemAddon {
    companion object : GearyAddon<FileSystem> {
        override fun FileSystem.install() {
        }
    }
}
