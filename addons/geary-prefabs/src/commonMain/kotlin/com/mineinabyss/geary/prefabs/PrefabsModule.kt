package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.prefabs.configuration.systems.*
import com.mineinabyss.geary.prefabs.systems.createInheritPrefabsOnLoadListener
import com.mineinabyss.geary.prefabs.systems.createTrackPrefabsByKeyListener
import com.mineinabyss.geary.serialization.SerializableComponents

data class PrefabsModule(
    val manager: PrefabManager,
    val loader: PrefabLoader,
)

class PrefabSources {
    val paths = mutableListOf<PrefabPath>()
}

val Prefabs = createAddon<PrefabSources, PrefabsModule>("Prefabs", {
    install(SerializableComponents)
    PrefabSources()
}) {
    val formats = geary.getAddon(SerializableComponents).formats
    val module = PrefabsModule(PrefabManager(), PrefabLoader(configuration, geary, formats, logger))

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
fun Namespaced.prefabs(configure: PrefabsDSL.() -> Unit) =
    setup.install(Prefabs) { PrefabsDSL(this, this@prefabs).configure() }
