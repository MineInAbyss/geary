package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.engine.Engine
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


public inline fun <reified T : GearyComponent> trait(): Trait<T> = Trait(T::class)

public class Trait<T : GearyComponent>(kClass: KClass<T>) : Accessor<T>(kClass)

public inline fun <reified T : GearyComponent> accessor(): Accessor<T> = Accessor(T::class)

public open class Accessor<T : GearyComponent>(kClass: KClass<T>) {
    internal val id: GearyComponentId = Engine.getComponentIdForClass(kClass)

    public operator fun get(entity: GearyEntityId): GearyComponent? = TODO("Matching systems to archetypes")
}

public class AccessorReader<T : GearyComponent>(public val accessor: Accessor<T>) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        TODO("Not yet implemented")
    }
}
