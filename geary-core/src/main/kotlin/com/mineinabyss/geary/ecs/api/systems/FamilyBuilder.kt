package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.holdsData
import com.mineinabyss.geary.ecs.engine.withInvertedRole
import com.mineinabyss.geary.ecs.query.*

public abstract class FamilyBuilder(public val engine: GearyEngine) {
    public abstract fun build(): Family

    public operator fun not(): MutableAndNotSelector = MutableAndNotSelector(engine, mutableListOf(this))
}

public fun family(engine: GearyEngine, init: MutableAndSelector.() -> Unit): AndSelector {
    return MutableAndSelector(engine).apply(init).build()
}

//TODO perhaps different hierarchy for leaves so we don't have to make mutable versions for nothing
public class MutableComponentLeaf(
    engine: GearyEngine,
    public var component: GearyComponentId
) : FamilyBuilder(engine) {
    override fun build(): ComponentLeaf = ComponentLeaf(component)
}

public class MutableRelationLeaf(
    engine: GearyEngine,
    public var relationDataType: RelationDataType,
    public val componentMustHoldData: Boolean = false
) : FamilyBuilder(engine) {
    override fun build(): RelationLeaf = RelationLeaf(relationDataType, componentMustHoldData)
}

public abstract class MutableSelector(engine: GearyEngine) : FamilyBuilder(engine) {
    protected abstract val elements: MutableList<FamilyBuilder>

    public val components: List<GearyComponentId> get() = _components
    public val componentsWithData: List<GearyComponentId> get() = _componentsWithData
    public val relationDataTypes: List<RelationDataType> get() = _relationDataTypes

    private val _components = mutableListOf<GearyComponentId>()
    private val _componentsWithData = mutableListOf<GearyComponentId>()
    private val _relationDataTypes = mutableListOf<RelationDataType>()

    protected fun add(element: FamilyBuilder) {
        elements += element
        if (element is MutableComponentLeaf) {
            val comp = element.component
            _components += comp
            if (comp.holdsData())
                _componentsWithData += comp
        }

        if (element is MutableRelationLeaf)
            _relationDataTypes += element.relationDataType
    }

    public fun not(init: MutableAndNotSelector.() -> Unit) {
        add(MutableAndNotSelector(engine).apply(init))
    }

    public fun and(init: MutableAndSelector.() -> Unit) {
        add(MutableAndSelector(engine).apply(init))
    }

    public fun or(init: MutableOrSelector.() -> Unit) {
        add(MutableOrSelector(engine).apply(init))
    }

    //TODO version of has that doesnt care about whether data is set
    public inline fun <reified T : GearyComponent> has() {
        or {
            has(engine.componentId<T>())
            has(engine.componentId<T>().withInvertedRole(HOLDS_DATA))
        }
    }

    public inline fun <reified T : GearyComponent> hasData() {
        has(engine.componentId<T>() or HOLDS_DATA)
    }

    public fun has(vararg componentIds: GearyComponentId) {
        has(componentIds)
    }

    public fun has(componentIds: Collection<GearyComponentId>) {
        componentIds.forEach {
            it.toRelation()?.let { relation ->
                add(MutableRelationLeaf(engine, relation.data))
            } ?: run {
                add(MutableComponentLeaf(engine, it))
            }
        }
    }

    public fun has(relationDataType: RelationDataType, componentMustHoldData: Boolean = false) {
        add(MutableRelationLeaf(engine, relationDataType, componentMustHoldData))
    }

    public fun has(relation: Relation) {
        has(relation.id)
    }
}

public open class MutableAndSelector(
    engine: GearyEngine,
    override val elements: MutableList<FamilyBuilder> = mutableListOf()
) : MutableSelector(engine) {
    override fun build(): AndSelector = AndSelector(elements.build())
}

public class MutableAndNotSelector(
    engine: GearyEngine,
    override val elements: MutableList<FamilyBuilder> = mutableListOf()
) : MutableSelector(engine) {
    override fun build(): AndNotSelector = AndNotSelector(elements.build())
}

public class MutableOrSelector(
    engine: GearyEngine,
    override val elements: MutableList<FamilyBuilder> = mutableListOf()
) : MutableSelector(engine) {
    override fun build(): OrSelector = OrSelector(elements.build())
}

private fun List<FamilyBuilder>.build() = map { it.build() }
