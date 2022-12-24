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
    val serializers: ComponentSerializers
    val formats: Formats

    companion object Plugin : GearyAddonWithDefault<SerializableComponents> {
        override fun default(): SerializableComponents = object : SerializableComponents {
            override val serializers = SerializersByMap()
            override val formats = SimpleFormats()
        }

        override fun SerializableComponents.install() {
            geary.pipeline.intercept(GearyPhase.INIT_COMPONENTS) {
                serializers
            }
        }
    }
}

val fileSystem by DI.observe<FileSystem>()

interface FileSystemAddon {
    companion object: GearyAddon<FileSystem> {
        override fun FileSystem.install() {
        }
    }
}
