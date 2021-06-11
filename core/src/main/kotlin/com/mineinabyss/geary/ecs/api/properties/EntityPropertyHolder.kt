package com.mineinabyss.geary.ecs.api.properties

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private typealias EntityExtension<T> = GearyEntity.() -> T?

public abstract class EntityPropertyHolder {
    private val dataMap = Long2ObjectOpenHashMap<List<Any>>()
    private val dataKey = mutableListOf<GearyComponentId>()
    private val extraData = Long2ObjectOpenHashMap<List<Any>>()
    private val extraProperties = mutableListOf<EntityExtension<*>>()

    public val propertiesEmpty: Boolean by lazy { extraProperties.isEmpty() && dataKey.isEmpty() }

    public operator fun <T : Any> EntityExtension<T>.provideDelegate(
        thisRef: Any?,
        prop: KProperty<*>
    ): PropertyDelegate<T> {
        return PropertyDelegate(this)
    }

    public inline fun <reified T : GearyComponent> get(): Accessor<T> =
        Accessor(componentId<T>() or HOLDS_DATA)

    public inline fun <T> GearyEntity.runWithProperties(run: GearyEntity.() -> T): T? {
        if (propertiesEmpty) return run()
        if (readProperties(this)) {
            return run().also { clear(this) }
        }
        return null
    }

    public fun readProperties(entity: GearyEntity): Boolean {
        val eid = entity.id.toLong()

        val (archetype, row) = Engine.getRecord(entity.id) ?: return false

        if (!extraData.containsKey(eid)) {
            extraData[eid] = extraProperties.map { it.invoke(entity) ?: return false }
        }

        // In an async situation where the same entity is iterated several times, it's better to just reuse potentially
        // outdated data than update it.
        if (!dataMap.containsKey(eid)) {
            val data = dataKey.map { id -> archetype[row, id] ?: return false }
            dataMap[eid] = data
        }
        return true
    }

    public fun clear(entity: GearyEntity) {
        val eid = entity.id.toLong()
        dataMap -= eid
        extraData -= eid
    }

    public inner class PropertyDelegate<T : Any>(
        property: EntityExtension<T>
    ) : ReadOnlyProperty<GearyEntity, T> {
        init {
            extraProperties += property
        }

        private val index: Int = extraProperties.lastIndex

        override fun getValue(thisRef: GearyEntity, property: KProperty<*>): T {
            return extraData[thisRef.id.toLong()]?.get(index) as T
        }
    }

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
