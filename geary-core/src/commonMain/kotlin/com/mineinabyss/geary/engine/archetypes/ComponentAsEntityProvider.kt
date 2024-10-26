package com.mineinabyss.geary.engine.archetypes

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.components.ReservedComponents
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.EntityProvider
import kotlin.reflect.KClassifier

class ComponentAsEntityProvider(
    val entityProvider: EntityProvider,
    val logger: Logger,
) : ComponentProvider {
    private val classToComponentMap = mutableMapOf<KClassifier, Long>()
//    private val classToComponentMapLock = Synchronizable() TODO async support necessary?

    init {
        createReservedComponents()
    }

    override fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId {
        val id = classToComponentMap.getOrElse(kClass) {
            return registerComponentIdForClass(kClass)
        }
        return id.toULong()
    }

    private fun registerComponentIdForClass(kClass: KClassifier): ComponentId {
        logger.v("Registering new component: $kClass")
        val compEntity = entityProvider.create()
//        compEntity.set(ComponentInfo(kClass), noEvent = true)
        classToComponentMap[kClass] = compEntity.toLong()
        return compEntity
    }

    private fun createReservedComponents() {
        logger.v("Creating reserved components")
        ReservedComponents.reservedComponents.forEach { (kClass, id) ->
            classToComponentMap[kClass] = id.toLong()
        }
    }
}
