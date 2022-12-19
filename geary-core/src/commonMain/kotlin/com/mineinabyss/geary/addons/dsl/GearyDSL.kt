package com.mineinabyss.geary.addons.dsl

import com.mineinabyss.geary.addons.dsl.serializers.SerializationAddon
import com.mineinabyss.geary.addons.GearyLoadPhase
import com.mineinabyss.geary.serialization.Formats
import com.mineinabyss.geary.systems.GearySystem
import kotlinx.serialization.modules.SerializersModule

interface GearyInstall<T> {
    fun install()
}
@GearyDSLMarker
interface GearyDSL {
    fun <T: GearyInstall<A>, A> install(addon: T, run: A.() -> Unit)
    fun <T: GearyInstall<A>, A> configure(addon: T, run: A.() -> Unit)
    /** Registers a [system]. */
    fun system(system: GearySystem)

    /** Registers a list of [systems]. */
    fun systems(vararg systems: GearySystem)

    fun formats(init: Formats.(SerializersModule) -> Unit)

    fun serialization(init: SerializationAddon.() -> Unit)

    /**
     * Allows defining actions that should run at a specific phase during startup
     *
     * Within its context, invoke a [GearyLoadPhase] to run something during it, ex:
     *
     * ```
     * GearyLoadPhase.ENABLE {
     *     // run code here
     * }
     * ```
     */
    fun on(phase: GearyLoadPhase, run: () -> Unit)

//    fun autoscan(pkg: String, init: AutoScanAddon.() -> Unit)
//    {
//        startup {
//            GearyLoadPhase.REGISTER_SERIALIZERS {
//                AutoScanAddon(
//                    pkg = pkg,
//                    serializationAddon = SerializationAddon(this@autoscan),
//                    gearyAddon = this@autoscan
//                ).init()
//            }
//        }
//    }

//    fun prefabs(init: PrefabsAddon.() -> Unit)
//    {
//        startup {
//            GearyLoadPhase.LOAD_PREFABS {
//                PrefabsAddon(this@prefabs).init()
//            }
//        }
//    }
}
