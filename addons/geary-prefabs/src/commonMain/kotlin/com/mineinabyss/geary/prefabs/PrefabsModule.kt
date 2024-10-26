package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.Addon
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.prefabs.configuration.systems.*
import com.mineinabyss.geary.prefabs.systems.createInheritPrefabsOnLoadListener
import com.mineinabyss.geary.prefabs.systems.createTrackPrefabsByKeyListener
import com.mineinabyss.geary.serialization.FileSystem
import com.mineinabyss.geary.serialization.SerializableComponents

interface PrefabsModule {
    val manager: PrefabManager
    val loader: PrefabLoader
}

class PrefabsBuilder {
    val paths: MutableList<PrefabPath> = mutableListOf()
}

val Prefabs
    get() = createAddon<PrefabsBuilder, PrefabsModule>("Prefabs", { PrefabsBuilder() }) {
        val formats = geary.getAddon(SerializableComponents).formats
        val module = object : PrefabsModule {
            override val manager = PrefabManager()
            override val loader: PrefabLoader = PrefabLoader(geary, formats, logger)
        }

        configuration.paths.forEach { module.loader.addSource(it) }

        systems {
            createInheritPrefabsOnLoadListener()
            createParseChildOnPrefabListener()
            createParseChildrenOnPrefabListener()
            createParseInstancesOnPrefabListener()
            createParseRelationOnPrefabListener()
            createParseRelationWithDataListener()
            createTrackPrefabsByKeyListener()
            createCopyToInstancesSystem()
            reEmitEvent()
        }

        entities {
            module.loader.loadOrUpdatePrefabs()
        }
        module
    }

@GearyDSL
fun Namespaced.prefabs(configure: PrefabsDSL.() -> Unit): Addon<PrefabsBuilder, PrefabsModule> =
    setup.install(Prefabs) { PrefabsDSL(this, setup.geary.getConfiguration(FileSystem), this@prefabs).configure() }
