package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyDSLMarker
import com.mineinabyss.geary.modules.GearyConfiguration
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.systems.ParseChildOnPrefab
import com.mineinabyss.geary.prefabs.configuration.systems.ParseChildrenOnPrefab
import com.mineinabyss.geary.prefabs.configuration.systems.ParseRelationOnPrefab
import com.mineinabyss.geary.prefabs.configuration.systems.ParseRelationWithDataSystem

val prefabs by geary.addons.observe<Prefabs>()

interface Prefabs {
    val manager: PrefabManager
    val loader: PrefabLoader

    companion object : GearyAddon<Prefabs, PrefabsConfiguration> {
        private val logger = geary.logger

        override fun default() = object : Prefabs {
            override val manager = PrefabManager()
            override val loader: PrefabLoader = PrefabLoader()
        }


        override fun Prefabs.install(geary: GearyModule) {
            geary.systems.add(
                ParseChildOnPrefab(),
                ParseChildrenOnPrefab(),
                ParseRelationOnPrefab(),
                ParseRelationWithDataSystem(),
            )
            geary.pipeline.intercept(GearyPhase.INIT_ENTITIES) {
                loader.loadPrefabs()
            }
        }
    }
}

@GearyDSLMarker
fun GearyConfiguration.prefabs(configure: Prefabs.() -> Unit) =
    install(Prefabs, configure)
