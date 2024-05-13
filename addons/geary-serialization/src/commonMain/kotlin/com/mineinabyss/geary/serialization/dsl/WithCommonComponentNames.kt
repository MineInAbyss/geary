package com.mineinabyss.geary.serialization.dsl

import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.observers.events.*

fun SerializableComponentsDSL.withCommonComponentNames() {
    namedComponent<OnAdd>("geary:on_add")
    namedComponent<OnSet>("geary:on_set")
    namedComponent<OnFirstSet>("geary:on_first_set")
    namedComponent<OnRemove>("geary:on_remove")
    namedComponent<OnUpdate>("geary:on_update")
    namedComponent<OnEntityRemoved>("geary:on_entity_removed")
    namedComponent<OnExtend>("geary:on_extend")
    namedComponent<ChildOf>("geary:child_of")
    namedComponent<InstanceOf>("geary:instance_of")
    namedComponent<Any>("geary:any")
}
