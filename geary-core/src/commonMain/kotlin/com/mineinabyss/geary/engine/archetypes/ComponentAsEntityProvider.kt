package com.mineinabyss.geary.engine.archetypes

import co.touchlab.kermit.Logger
import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize
import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import kotlin.reflect.KClassifier

class ComponentAsEntityProvider(
    val entityProvider: EntityProvider,
    val logger: Logger,
) : ComponentProvider {
    override val types: Components = Components(this)
    private val classToComponentMap = mutableMapOf<KClassifier, Long>()
    private val classToComponentMapLock = Synchronizable()

    internal fun createComponentInfo() {
        logger.v("Registering ComponentInfo component")
        //Register an entity for the ComponentInfo component, otherwise getComponentIdForClass does a StackOverflow
        val componentInfo = entityProvider.create()
        classToComponentMap[ComponentInfo::class] = componentInfo.toLong()
        //TODO is this actually necessary?
//        componentInfo.set(ComponentInfo(ComponentInfo::class), noEvent = true)
    }

    override fun getOrRegisterComponentIdForClass(kClass: KClassifier): ComponentId =
        classToComponentMapLock.synchronize {
            val id = classToComponentMap.getOrElse(kClass) {
                return@synchronize registerComponentIdForClass(kClass)
            }
            return@synchronize id.toULong()
        }

    private fun registerComponentIdForClass(kClass: KClassifier): ComponentId {
        logger.v("Registering new component: $kClass")
        val compEntity = entityProvider.create()
//        compEntity.set(ComponentInfo(kClass), noEvent = true)
        classToComponentMap[kClass] = compEntity.toLong()
        return compEntity
    }
}
