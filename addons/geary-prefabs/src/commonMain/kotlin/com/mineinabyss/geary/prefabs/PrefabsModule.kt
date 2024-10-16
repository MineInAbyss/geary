package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.Addon
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.prefabs.configuration.systems.*
import com.mineinabyss.geary.prefabs.systems.createInheritPrefabsOnLoadListener
import com.mineinabyss.geary.prefabs.systems.createTrackPrefabsByKeyListener
import com.mineinabyss.geary.serialization.FileSystem
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.formats.Formats

interface PrefabsModule {
    val manager: PrefabManager
    val loader: PrefabLoader
}

val Prefabs get() = createAddon<PrefabsModule>("Prefabs", {
    val formats = getAddon(SerializableComponents).formats
    object : PrefabsModule {
        override val manager = PrefabManager()
        override val loader: PrefabLoader = PrefabLoader(this@createAddon, formats, logger)
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
fun Namespaced.prefabs(configure: PrefabsDSL.() -> Unit): Addon<PrefabsModule, PrefabsModule> =
    setup.install(Prefabs) { PrefabsDSL(setup.geary.getAddon(FileSystem), loader, this@prefabs).configure() }
