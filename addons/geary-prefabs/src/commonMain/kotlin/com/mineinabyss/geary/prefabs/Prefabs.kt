package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Addon
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.addons.install
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.entities
import com.mineinabyss.geary.prefabs.configuration.systems.*
import com.mineinabyss.geary.prefabs.systems.createInheritPrefabsOnLoadListener
import com.mineinabyss.geary.prefabs.systems.createTrackPrefabsByKeyListener
import com.mineinabyss.idofront.di.DI

val prefabs by DI.observe<Prefabs>()

class Prefabs(
    val manager: PrefabManager = PrefabManager(),
    val loader: PrefabLoader = PrefabLoader(),
) {

    companion object : Addon<GearyModule, Prefabs, Prefabs> {
        override fun install(app: GearyModule, configure: Prefabs.() -> Unit): Prefabs = app.run {
            createInheritPrefabsOnLoadListener()
            createParseChildOnPrefabListener()
            createParseChildrenOnPrefabListener()
            createParseInstancesOnPrefabListener()
            createParseRelationOnPrefabListener()
            createParseRelationWithDataListener()
            createTrackPrefabsByKeyListener()
            createCopyToInstancesSystem()
            bindEntityObservers()
            reEmitEvent()

            val prefabs = Prefabs().apply(configure)

            entities {
                prefabs.loader.loadOrUpdatePrefabs()
            }

            return prefabs
        }
    }
}

@GearyDSL
fun Namespaced.prefabs(configure: PrefabsDSL.() -> Unit) =
    module.install(Prefabs).also { PrefabsDSL(this).configure() }
