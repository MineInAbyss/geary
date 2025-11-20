package com.mineinabyss.geary.addons.dsl

//data class AddonSetup<Configuration>(
//    val name: String,
//    val configuration: Configuration,
//    val application: KoinApplication,
//): KoinComponent {
//    override fun getKoin(): Koin = application.koin
//    val logger = get<Logger>().withTag(name)
//    val geary: Geary = Geary(application, logger)
//
//    /** Runs a block during [GearyPhase.INIT_COMPONENTS] */
//    fun components(configure: Geary.() -> Unit) {
//        on(GearyPhase.INIT_COMPONENTS) {
//            configure(geary)
//        }
//    }
//
//    /** Runs a block during [GearyPhase.INIT_SYSTEMS] */
//    fun systems(configure: Geary.() -> Unit) {
//        on(GearyPhase.INIT_SYSTEMS) {
//            configure(geary)
//        }
//    }
//
//    /** Runs a block during [GearyPhase.INIT_ENTITIES] */
//    fun entities(configure: Geary.() -> Unit) {
//        on(GearyPhase.INIT_ENTITIES) {
//            configure(geary)
//        }
//    }
//
//    fun onStart(run: Geary.() -> Unit) {
//        on(GearyPhase.ENABLE) {
//            run(geary)
//        }
//    }
//
//    /**
//     * Allows defining actions that should run at a specific phase during startup
//     *
//     * Within its context, invoke a [GearyPhase] to run something during it, ex:
//     *
//     * ```
//     * GearyLoadPhase.ENABLE {
//     *     // run code here
//     * }
//     * ```
//     */
//    fun on(phase: GearyPhase, run: () -> Unit) {
//        geary.pipeline.runOnOrAfter(phase, run)
//    }
//
//    fun inject(vararg modules: Module) {
//        application.modules(*modules)
//    }
//}
//
//@JvmName("createAddon0")
//fun createAddon(
//    name: String,
//    init: AddonSetup<Unit>.() -> Unit,
//): Addon<Unit, Unit> = Addon(name, { }) {
//    init(AddonSetup(name, it, application))
//}
//
//@JvmName("createAddon1")
//fun <Conf> createAddon(
//    name: String,
//    configuration: GearySetup.() -> Conf,
//    init: AddonSetup<Conf>.() -> Unit = {},
//): Addon<Conf, Conf> = Addon(name, configuration) { conf ->
//    init(AddonSetup(name, conf, application))
//    conf
//}
//
//@JvmName("createAddon2")
//fun <Conf, Inst> createAddon(
//    name: String,
//    configuration: GearySetup.() -> Conf,
//    init: AddonSetup<Conf>.() -> Inst,
//): Addon<Conf, Inst> {
//    return Addon(name, configuration) {
//        init(AddonSetup(name, it, application))
//    }
//}
