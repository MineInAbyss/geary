package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.prefabs.configuration.systems.*
import com.mineinabyss.geary.prefabs.systems.createInheritPrefabsOnLoadListener
import com.mineinabyss.geary.prefabs.systems.createTrackPrefabsByKeyListener

interface PrefabsModule {
    val manager: PrefabManager
    val loader: PrefabLoader
}

val Prefabs = createAddon<PrefabsModule>("Prefabs", {
    object : PrefabsModule {
        override val manager = PrefabManager()
        override val loader: PrefabLoader = PrefabLoader()
    }
}) {
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
        configuration.loader.loadOrUpdatePrefabs()
    }
}

@GearyDSL
fun Namespaced.prefabs(configure: PrefabsDSL.() -> Unit) =
    setup.install(Prefabs) { PrefabsDSL(loader, this@prefabs).configure() }
