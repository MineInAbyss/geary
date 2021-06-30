package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.query.*

public abstract class FamilyBuilder {
    public abstract fun build(): Family

    public operator fun not(): MutableAndNotSelector = MutableAndNotSelector(mutableListOf(this))
}

public fun family(init: MutableAndSelector.() -> Unit): AndSelector {
    return MutableAndSelector().apply(init).build()
}

public class MutableComponentLeaf(
    public var component: GearyComponentId
) : FamilyBuilder() {
    override fun build(): ComponentLeaf = ComponentLeaf(component)
}

public class MutableRelationLeaf(
    public var relation: RelationParent
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

    //TODO version of has that doesnt care about whether data is set
    public inline fun <reified T : GearyComponent> has() {
        or {
            has(componentId<T>())
            has(componentId<T>() xor HOLDS_DATA)
        }
    }

    public inline fun <reified T : GearyComponent> hasData() {
        has(componentId<T>() or HOLDS_DATA)
    }

    public fun has(vararg componentIds: GearyComponentId) {
        has(componentIds)
    }

    public fun has(componentIds: Collection<GearyComponentId>) {
        componentIds.forEach {
            it.toRelation()?.let { relation ->
                elements += MutableRelationLeaf(relation.parent)
            } ?: run {
                elements += MutableComponentLeaf(it)
            }
        }
    }

    public fun has(relationParent: RelationParent) {
        elements += MutableRelationLeaf(relationParent)
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
