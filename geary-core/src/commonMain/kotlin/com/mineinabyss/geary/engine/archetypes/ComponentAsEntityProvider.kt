package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.modules.geary
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.reflect.KClassifier

class ComponentAsEntityProvider : ComponentProvider {
    private val entityProvider get() = geary.entityProvider
    private val logger get() = geary.logger

    private val classToComponentMap = mutableMapOf<KClassifier, Long>()
    private val classToComponentMapLock = SynchronizedObject()

    internal fun createComponentInfo() {
        logger.v("Registering ComponentInfo component")
        //Register an entity for the ComponentInfo component, otherwise getComponentIdForClass does a StackOverflow
        val componentInfo = entityProvider.create()
        classToComponentMap[ComponentInfo::class] = componentInfo.id.toLong()
        componentInfo.set(ComponentInfo(ComponentInfo::class), noEvent = true)
    }

    override fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId =
        synchronized(classToComponentMapLock) {
            val id = classToComponentMap.getOrElse(kClass) {
                return registerComponentIdForClass(kClass)
            }
            return id.toULong()
        }

    private fun registerComponentIdForClass(kClass: KClassifier): ComponentId {
        logger.v("Registering new component: $kClass")
        val compEntity = entityProvider.create()
        compEntity.set(ComponentInfo(kClass), noEvent = true)
        classToComponentMap[kClass] = compEntity.id.toLong()
        return compEntity.id
    }
}
