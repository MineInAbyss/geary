package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.maps.ClassToComponentMap
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.EntityProvider
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.reflect.KClass

class ComponentAsEntityProvider : ComponentProvider {
    private val entityProvider: EntityProvider get() = geary.entityProvider

    private val classToComponentMap = ClassToComponentMap()
    private val classToComponentMapLock = SynchronizedObject()

    internal fun createComponentInfo() {
        //Register an entity for the ComponentInfo component, otherwise getComponentIdForClass does a StackOverflow
        val componentInfo = entityProvider.newEntity()
        classToComponentMap[ComponentInfo::class] = componentInfo.id
        componentInfo.set(ComponentInfo(ComponentInfo::class), noEvent = true)
    }

    override fun getOrRegisterComponentIdForClass(kClass: KClass<*>): ComponentId =
        synchronized(classToComponentMapLock) {
            val id = classToComponentMap[kClass]
            if (id == (-1L).toULong()) return registerComponentIdForClass(kClass)
            return id
        }

    private fun registerComponentIdForClass(kClass: KClass<*>): ComponentId {
        val compEntity = entityProvider.newEntity(initialComponents = listOf(ComponentInfo(kClass)))
        classToComponentMap[kClass] = compEntity.id
        return compEntity.id
    }
}
