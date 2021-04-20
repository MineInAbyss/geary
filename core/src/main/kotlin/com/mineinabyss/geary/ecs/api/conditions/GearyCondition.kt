package com.mineinabyss.geary.ecs.api.conditions

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A serializable condition that can be checked against any ECS entity.
 */
@Serializable
public abstract class GearyCondition {
    @Transient
    private val dataMap = Long2ObjectOpenHashMap<List<Any>>()

    @Transient
    private val dataKey = mutableListOf<GearyComponentId>()

    public fun metFor(entity: GearyEntity): Boolean {
        if(dataKey.isEmpty()) return entity.check()

        val (archetype, row) = Engine.getRecord(entity.id) ?: return false

        // In an async situation where the same entity is iterated several times, it's better to just reuse potentially
        // outdated data than update it.
        if (!dataMap.containsKey(entity.id.toLong())) {
            val data = dataKey.map { id -> archetype[row, id] ?: return false }
            dataMap[entity.id.toLong()] = data
        }

        return entity.check().also {
            dataMap.remove(entity.id.toLong())
        }
    }

    protected abstract fun GearyEntity.check(): Boolean

    public inline fun <reified T : GearyComponent> get(): Accessor<T> =
        Accessor(componentId<T>() or HOLDS_DATA)

    public inner class Accessor<T : GearyComponent>(
        private val componentId: GearyComponentId
    ) : ReadOnlyProperty<GearyEntity, T> {
        init {
            dataKey.add(componentId)
        }

        private val index: Int = dataKey.lastIndex


        //TODO implement contracts for smart cast if Kotlin ever does so for lazy (this should essentially be identical)
        override fun getValue(thisRef: GearyEntity, property: KProperty<*>): T {
            return dataMap[thisRef.id.toLong()]!![index] as T
        }
    }
}
