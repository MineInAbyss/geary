package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.components.Persists
import com.mineinabyss.geary.serialization.dsl.SerializableComponentsDSL
import com.mineinabyss.geary.serialization.dsl.builders.ComponentSerializersBuilder
import com.mineinabyss.geary.serialization.dsl.builders.FormatsBuilder
import com.mineinabyss.geary.serialization.formats.Formats
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.idofront.di.DI

val serializableComponents by DI.observe<SerializableComponents>()

interface SerializableComponents {
    val serializers: ComponentSerializers
    val formats: Formats
    val persists: ComponentId

    interface Builder {
        val serializersBuilder: ComponentSerializersBuilder
        val formatsBuilder: FormatsBuilder
    }

    companion object : GearyAddonWithDefault<Builder> {
        override fun default(): Builder = object : Builder {
            override val serializersBuilder = ComponentSerializersBuilder()
            override val formatsBuilder = FormatsBuilder()
        }

        override fun Builder.install() {
            SerializableComponentsDSL(this).apply {
                components {
                    component(GearyEntitySerializer)
                }
            }
            geary.pipeline.runOnOrAfter(GearyPhase.ADDONS_CONFIGURED) {
                DI.add<SerializableComponents>(object : SerializableComponents {
                    override val serializers = serializersBuilder.build()
                    override val formats = formatsBuilder.build(serializers)
                    override val persists: ComponentId = componentId<Persists>()
                })
            }
        }
    }
}
