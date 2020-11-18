package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.geary
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.idofront.plugin.getService
import net.onedaybeard.bitvector.BitVector
import org.bukkit.NamespacedKey

public interface Engine {
    public companion object : Engine by getService() {
        public val componentsKey: NamespacedKey = NamespacedKey("geary", "components")
    }

    public fun getNextId(): Int

    public fun addSystem(system: TickingSystem): Boolean

    public fun getComponentsFor(id: Int): Set<GearyComponent>
    public fun getComponentFor(kClass: ComponentClass, id: Int): GearyComponent?
    public fun hasComponentFor(kClass: ComponentClass, id: Int): Boolean
    public fun removeComponentFor(kClass: ComponentClass, id: Int)
    public fun <T : GearyComponent> addComponentFor(id: Int, component: T): T

    public fun removeEntity(entity: GearyEntity)

    //TODO this shouldn't be in interface but currently required for inline functions in [Iteration]
    public fun getBitsMatching(vararg components: ComponentClass, andNot: Array<out ComponentClass> = emptyArray()): BitVector

    //some helpers
    public fun addSystems(vararg systems: TickingSystem) {
        systems.forEach { addSystem(it) }
    }

    public fun addComponentsFor(id: Int, components: Set<GearyComponent>) {
        components.forEach { addComponentFor(id, it) }
    }
}

public inline fun Engine.entity(run: GearyEntity.() -> Unit): GearyEntity = geary(getNextId(), run)
