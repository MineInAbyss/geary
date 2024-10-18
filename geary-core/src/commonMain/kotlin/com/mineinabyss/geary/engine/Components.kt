package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.CouldHaveChildren
import com.mineinabyss.geary.components.KeepEmptyArchetype
import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.events.*

class Components(
    comp: ComponentProvider,
) {
    val any = comp.id<Any>()
    val suppressRemoveEvent = comp.id<SuppressRemoveEvent>()
    val couldHaveChildren = comp.id<CouldHaveChildren>()
    val observer = comp.id<Observer>()
    val onAdd = comp.id<OnAdd>()
    val onSet = comp.id<OnSet>()
    val onFirstSet = comp.id<OnFirstSet>()
    val onUpdate = comp.id<OnUpdate>()
    val onRemove = comp.id<OnRemove>()
    val onExtend = comp.id<OnExtend>()
    val onEntityRemoved = comp.id<OnEntityRemoved>()
    val childOf = comp.id<ChildOf>()
    val instanceOf = comp.id<InstanceOf>()
    val noInherit = comp.id<NoInherit>()
    val keepEmptyArchetype = comp.id<KeepEmptyArchetype>()
}
