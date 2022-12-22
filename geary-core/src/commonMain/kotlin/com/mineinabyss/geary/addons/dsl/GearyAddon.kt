package com.mineinabyss.geary.addons.dsl

import com.mineinabyss.geary.modules.GearyModule

/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
//@GearyDSLMarker
//class GearyAddon(
//    val namespace: String,
//    val classLoader: ClassLoader
//) : GearyDSL {
//
//    override fun system(system: GearySystem) {
//        geary.systems.add(system)
//    }
//
//    override fun systems(vararg systems: GearySystem) {
//        systems.forEach { system(it) }
//    }
//
//    override fun serialization(init: SerializationAddon.() -> Unit) = on(GearyLoadPhase.REGISTER_SERIALIZERS) {
//            SerializationAddon(this@GearyAddon).init()
//    }
//
//    override fun formats(init: Formats.(SerializersModule) -> Unit) = on(GearyLoadPhase.REGISTER_FORMATS) {
//        geary.formats.init(geary.serializers.module)
//    }
//
//    /**
//     * Allows defining actions that should run at a specific phase during startup
//     *
//     * Within its context, invoke a [GearyLoadPhase] to run something during it, ex:
//     *
//     * ```
//     * GearyLoadPhase.ENABLE {
//     *     // run code here
//     * }
//     * ```
//     */
//    override fun on(phase: GearyLoadPhase, run: () -> Unit) {
//        addons.manager.add(phase, run)
//    }
//}
interface GearyAddon<Module, Conf> {
    fun default(): Module
    fun Module.install(geary: GearyModule)
}
