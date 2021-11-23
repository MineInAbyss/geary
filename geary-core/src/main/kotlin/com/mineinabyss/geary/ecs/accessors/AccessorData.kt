package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.GearyEngine
import kotlin.reflect.KClass

/**
 * Some raw data [Accessor]s process to eventually build up to a [ResultScope].
 *
 * Extends [ArchetypeCacheScope] because anything in this scope should have access to
 * properties cached per archetype.
 *
 * @see Accessor
 */
public class RawAccessorDataScope(
    archetype: Archetype,
    perArchetypeData: List<List<Any?>>,
    public val row: Int,
    public val entity: GearyEntity,
) : ArchetypeCacheScope(archetype, perArchetypeData)

/**
 * Scope provided for accessor caching by archetype.
 */
public open class ArchetypeCacheScope(
    public val archetype: Archetype,
    public val perArchetypeData: List<List<Any?>>,
)


/**
 * Stores data which is formatted
 */
public class ResultScope(
    public val entity: GearyEntity,
    internal val data: List<*>,
    engine: GearyEngine
) : GearyAccessorScopeDelegate(engine)


public open class GearyAccessorScopeDelegate(public val engine: GearyEngine) {
    public inline fun <reified T : GearyComponent> GearyEntity.testSet(
        component: T,
        kClass: KClass<out T> = T::class
    ): T {
        engine.setComponentFor(id, componentId(kClass), component)
        return component
    }
}