package com.mineinabyss.geary.engine

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.components.ReservedComponents
import com.mineinabyss.geary.datatypes.ComponentId
import kotlin.reflect.KClassifier

actual class ComponentProvider actual constructor(
    private val entityProvider: EntityProvider,
    private val logger: Logger,
) {
    private val classToComponentIdValue = object : ClassValue<IdContainer>() {
        override fun computeValue(p0: Class<*>): IdContainer {
            return IdContainer(registerComponentIdForClass(p0))
        }
    }

    init {
        createReservedComponents()
    }

    actual fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId {
        return getOrRegisterComponentIdForClass(kClass.javaClass)
    }

    fun getOrRegisterComponentIdForClass(jClass: Class<*>): ComponentId {
        return classToComponentIdValue.get(jClass).id.toULong()
    }

    private fun registerComponentIdForClass(kClass: Class<*>): ComponentId {
        logger.v { "Registering new component: $kClass" }
        val compEntity = entityProvider.create()
        return compEntity
    }

    private fun createReservedComponents() {
        logger.v { "Creating reserved components" }
        ReservedComponents.reservedComponents.forEach { (kClass, id) ->
            getOrRegisterComponentIdForClass(kClass.java)
        }
    }

    private class IdContainer(id: ULong) {
        @JvmField
        val id = id.toLong()
    }
}