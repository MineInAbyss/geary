package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.holdsData
import com.mineinabyss.geary.ecs.query.*

public abstract class FamilyBuilder {
    public abstract fun build(): Family

    public operator fun not(): MutableAndNotSelector = MutableAndNotSelector(mutableListOf(this))
}

public fun family(init: MutableAndSelector.() -> Unit): AndSelector {
    return MutableAndSelector().apply(init).build()
}

//TODO perhaps different hierarchy for leaves so we don't have to make mutable versions for nothing
public class MutableComponentLeaf(
    public var component: GearyComponentId
) : FamilyBuilder() {
    override fun build(): ComponentLeaf = ComponentLeaf(component)
}

public class MutableRelationLeaf(
    public var relationParent: RelationParent,
    public val componentMustHoldData: Boolean = false
) : FamilyBuilder() {
    override fun build(): RelationLeaf = RelationLeaf(relationParent, componentMustHoldData)
}

public abstract class MutableSelector : FamilyBuilder() {
    protected abstract val elements: MutableList<FamilyBuilder>

    public val components: List<GearyComponentId> get() = _components
    public val componentsWithData: List<GearyComponentId> get() = _componentsWithData
    public val relationParents: List<RelationParent> get() = _relationParents

    private val _components = mutableListOf<GearyComponentId>()
    private val _componentsWithData = mutableListOf<GearyComponentId>()
    private val _relationParents = mutableListOf<RelationParent>()

    protected fun add(element: FamilyBuilder) {
        elements += element
        if (element is MutableComponentLeaf) {
            val comp = element.component
            _components += comp
            if (comp.holdsData())
                _componentsWithData += comp
        }

        if (element is MutableRelationLeaf)
            _relationParents += element.relationParent
    }

    public fun not(init: MutableAndNotSelector.() -> Unit) {
        add(MutableAndNotSelector().apply(init))
    }

    public fun and(init: MutableAndSelector.() -> Unit) {
        add(MutableAndSelector().apply(init))
    }

    public fun or(init: MutableOrSelector.() -> Unit) {
        add(MutableOrSelector().apply(init))
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
                add(MutableRelationLeaf(relation.parent))
            } ?: run {
                add(MutableComponentLeaf(it))
            }
        }
    }

    public fun has(relationParent: RelationParent, componentMustHoldData: Boolean = false) {
        add(MutableRelationLeaf(relationParent, componentMustHoldData))
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
