package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.geary
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.idofront.plugin.getService
import net.onedaybeard.bitvector.BitVector
import org.bukkit.NamespacedKey

interface Engine {
    companion object : Engine by getService() {
        val componentsKey = NamespacedKey("geary", "components")
    }

    fun getNextId(): Int

    fun addSystem(system: TickingSystem): Boolean

    fun getComponentsFor(id: Int): Set<GearyComponent>
    fun getComponentFor(kClass: ComponentClass, id: Int): GearyComponent?
    fun hasComponentFor(kClass: ComponentClass, id: Int): Boolean
    fun removeComponentFor(kClass: ComponentClass, id: Int)
    fun <T : GearyComponent> addComponentFor(id: Int, component: T): T

    fun removeEntity(id: GearyEntity)

    //TODO this shouldn't be in interface but currently required for inline functions in [Iteration]
    fun getBitsMatching(vararg components: ComponentClass, andNot: Array<out ComponentClass> = emptyArray()): BitVector

    //some helpers
    fun addSystems(vararg systems: TickingSystem) = systems.forEach { addSystem(it) }
    fun addComponentsFor(id: Int, components: Set<GearyComponent>) = components.forEach { addComponentFor(id, it) }
}

inline fun Engine.entity(run: GearyEntity.() -> Unit): GearyEntity = geary(getNextId(), run)
