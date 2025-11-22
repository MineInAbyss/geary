package com.mineinabyss.geary.autoscan

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.SerializableComponentsModule

//@GearyDSL
//fun GearySetup.autoscan(
//    classLoader: ClassLoader,
//    vararg limitToPackages: String,
//    configure: AutoScannerDSL.() -> Unit,
//) = install(AutoScanAddon) { AutoScannerDSL(this@autoscan, this, classLoader, limitToPackages.toList()).configure() }

@GearyDSL
class AutoScanner(
    private val world: Geary,
    private val logger: Logger,
    private val serializableComponents: SerializableComponentsModule,
) {
    fun scan(
        classLoader: ClassLoader,
        limitToPackages: List<String>,
        block: AutoScanDefinition.() -> Unit,
    ) {
        AutoScanDefinition(
            logger,
            classLoader,
            limitToPackages,
            serializableComponents,
        ).apply(block)
    }
}
