package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyComponentId
import com.mineinabyss.geary.ecs.GearyEntityId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.engine.GearyEngine
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


public inline fun <reified T : GearyComponent> trait(): Trait<T> = Trait(T::class)

public class Trait<T : GearyComponent>(kClass: KClass<T>) : Accessor<T>(kClass)

public inline fun <reified T : GearyComponent> accessor(): Accessor<T> = Accessor(T::class)

public open class Accessor<T : GearyComponent>(kClass: KClass<T>) {
    //TODO no cast
    internal val id: GearyComponentId = Engine.getComponentIdForClass(kClass)
    internal val componentArray = (Engine as GearyEngine).getComponentArrayFor(id)

    public operator fun get(entity: GearyEntityId): GearyComponent? = componentArray[entity.toInt()]
}

public class AccessorReader<T : GearyComponent>(public val accessor: Accessor<T>): ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        TODO("Not yet implemented")
    }
}
