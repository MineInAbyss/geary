package com.mineinabyss.geary.engine

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.components.ReservedComponents
import com.mineinabyss.geary.datatypes.ComponentId
import kotlin.reflect.KClassifier

actual class ComponentProvider actual constructor(
    private val entityProvider: EntityProvider,
    private val logger: Logger,
) {
    private val classToComponentMap = mutableMapOf<KClassifier, Long>()

    init {
        createReservedComponents()
    }

    actual fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId {
        val id = classToComponentMap.getOrElse(kClass) {
            return registerComponentIdForClass(kClass)
        }
        return id.toULong()
    }

    private fun registerComponentIdForClass(kClass: KClassifier): ComponentId {
        logger.v { "Registering new component: $kClass" }
        val compEntity = entityProvider.create()
        classToComponentMap[kClass] = compEntity.toLong()
        return compEntity
    }

    private fun createReservedComponents() {
        logger.v { "Creating reserved components" }
        ReservedComponents.reservedComponents.forEach { (kClass, id) ->
            classToComponentMap[kClass] = id.toLong()
        }
    }
}
