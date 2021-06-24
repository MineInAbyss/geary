package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.query.*

public abstract class FamilyBuilder {
    public abstract fun build(): Family

    public operator fun not(): MutableAndNotSelector = MutableAndNotSelector(mutableListOf(this))
}

public fun family(init: MutableAndSelector.() -> Unit): Family {
    return MutableAndSelector().apply(init).build()
}

public class MutableComponentLeaf(
    public var component: GearyComponentId
) : FamilyBuilder() {
    override fun build(): ComponentLeaf = ComponentLeaf(component)
}

public class MutableRelationLeaf(
    public var relation: Relation
) : FamilyBuilder() {
    override fun build(): RelationLeaf = RelationLeaf(relation)
}

public abstract class MutableSelector : FamilyBuilder() {
    public abstract val elements: MutableList<FamilyBuilder>

    public fun not(init: MutableAndNotSelector.() -> Unit) {
        elements += MutableAndNotSelector().apply(init)
    }

    public fun and(init: MutableAndSelector.() -> Unit) {
        elements += MutableAndSelector().apply(init)
    }

    public fun or(init: MutableOrSelector.() -> Unit) {
        elements += MutableOrSelector().apply(init)
    }


    public inline fun <reified T : GearyComponent> has() {
        has(componentId<T>())
    }

    public fun has(vararg componentId: GearyComponentId) {
        componentId.forEach {
            elements += MutableComponentLeaf(it)
        }
    }

    public fun has(componentId: Relation) {
        elements += MutableRelationLeaf(componentId)
    }
}

public open class MutableAndSelector(
    override val elements: MutableList<FamilyBuilder> = mutableListOf()
) : MutableSelector() {
    override fun build(): AndSelector = AndSelector(elements.build())
}

public class MutableAndNotSelector(
    override val elements: MutableList<FamilyBuilder> = mutableListOf()
) : MutableSelector() {
    override fun build(): AndNotSelector = AndNotSelector(elements.build())
}

public class MutableOrSelector(
    override val elements: MutableList<FamilyBuilder> = mutableListOf()
) : MutableSelector() {
    override fun build(): OrSelector = OrSelector(elements.build())
}

private fun List<FamilyBuilder>.build() = map { it.build() }
