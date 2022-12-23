package com.mineinabyss.geary.serialization

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.formats.Formats
import com.mineinabyss.geary.serialization.formats.SimpleFormats
import okio.FileSystem

val serializableComponents by DI.observe<SerializableComponents>()

interface SerializableComponents {
    val serializers: Serializers
    val formats: Formats
    val fileSystem: FileSystem

    companion object Plugin : GearyAddonWithDefault<SerializableComponents> {
        override fun default(): SerializableComponents = object : SerializableComponents {
            override val serializers = SerializersByMap()
            override val formats = SimpleFormats()
        }

        override fun SerializableComponents.install() {
            geary.pipeline.intercept(GearyPhase.INIT_COMPONENTS) {

            }
        }
    }
}

object FileSystemAddon : GearyAddon<FileSystem> {
    override fun FileSystem.install() {
        TODO("Not yet implemented")
    }
}
