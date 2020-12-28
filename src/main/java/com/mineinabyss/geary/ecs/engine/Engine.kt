package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.geary
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.idofront.plugin.getService
import net.onedaybeard.bitvector.BitVector
import org.bukkit.NamespacedKey
import kotlin.reflect.KClass

public interface Engine {
    public companion object : Engine by getService() {
        //TODO all other components use key gearyecs, change it here for consistency
        public val componentsKey: NamespacedKey = NamespacedKey("geary", "components")
    }

    public fun getNextId(): Int

    public fun addSystem(system: TickingSystem): Boolean

    public fun getComponentsFor(id: Int): Set<GearyComponent>
    public fun <T : GearyComponent> getComponentFor(kClass: KClass<T>, id: Int): T?

    /** Checks whether [id] holds a [component type][kClass], without regards for whether or not it's active. */
    public fun holdsComponentFor(kClass: ComponentClass, id: Int): Boolean

    /** Checks whether [id] has an active [component type][kClass] */
    public fun hasComponentFor(kClass: ComponentClass, id: Int): Boolean

    /**
     * @return Whether the component was present before removal.
     */
    public fun removeComponentFor(kClass: ComponentClass, id: Int): Boolean
    public fun <T : GearyComponent> addComponentFor(kClass: ComponentClass, id: Int, component: T): T
    public fun enableComponentFor(kClass: ComponentClass, id: Int)
    public fun disableComponentFor(kClass: ComponentClass, id: Int)

    public fun removeEntity(entity: GearyEntity)

    //TODO this shouldn't be in interface but currently required for inline functions in [Iteration]
    public fun getBitsMatching(
        vararg components: ComponentClass,
        andNot: Array<out ComponentClass> = emptyArray(),
        checkConditions: Boolean = true
    ): BitVector

    //some helpers
    public fun addSystems(vararg systems: TickingSystem) {
        systems.forEach { addSystem(it) }
    }

    public fun addComponentsFor(id: Int, components: Set<GearyComponent>) {
        components.forEach { addComponentFor(it::class, id, it) }
    }
}

public inline fun Engine.entity(run: GearyEntity.() -> Unit): GearyEntity = geary(getNextId(), run)
