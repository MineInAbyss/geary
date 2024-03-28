package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.systems.*
import com.mineinabyss.geary.prefabs.systems.createInheritPrefabsOnLoadListener
import com.mineinabyss.geary.prefabs.systems.createTrackPrefabsByKeyListener
import com.mineinabyss.idofront.di.DI

val prefabs by DI.observe<Prefabs>()

interface Prefabs {
    val manager: PrefabManager
    val loader: PrefabLoader

    companion object : GearyAddonWithDefault<Prefabs> {
        override fun default() = object : Prefabs {
            override val manager = PrefabManager()
            override val loader: PrefabLoader = PrefabLoader()
        }


        override fun Prefabs.install() {
            geary.run {
                createInheritPrefabsOnLoadListener()
                createParseChildOnPrefabListener()
                createParseChildrenOnPrefabListener()
                createParseInstancesOnPrefabListener()
                createParseRelationOnPrefabListener()
                createParseRelationWithDataListener()
                createTrackPrefabsByKeyListener()
                createCopyToInstancesSystem()
            }
            geary.pipeline.runOnOrAfter(GearyPhase.INIT_ENTITIES) {
                loader.loadOrUpdatePrefabs()
            }
        }
    }
}

@GearyDSL
fun Namespaced.prefabs(configure: PrefabsDSL.() -> Unit) =
    gearyConf.install(Prefabs).also { PrefabsDSL(this).configure() }
