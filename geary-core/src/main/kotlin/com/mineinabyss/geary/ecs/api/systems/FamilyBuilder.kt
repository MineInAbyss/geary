package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.*
import com.mineinabyss.geary.ecs.query.*
import kotlin.reflect.KType

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

public class MutableRelationValueLeaf(
    public var relationValueId: RelationValueId,
    public val componentMustHoldData: Boolean = false
) : FamilyBuilder() {
    override fun build(): RelationValueLeaf = RelationValueLeaf(relationValueId, componentMustHoldData)
}

public class MutableRelationKeyLeaf(
    public var relationKeyId: GearyComponentId,
    public val componentMustHoldData: Boolean = false
) : FamilyBuilder() {
    override fun build(): RelationKeyLeaf = RelationKeyLeaf(relationKeyId)
}


public abstract class MutableSelector : FamilyBuilder() {
    protected abstract val elements: MutableList<FamilyBuilder>

    public val components: List<GearyComponentId> get() = _components
    public val componentsWithData: List<GearyComponentId> get() = _componentsWithData
    public val relationValueIds: List<RelationValueId> get() = _relationValueIds

    private val _components = mutableListOf<GearyComponentId>()
    private val _componentsWithData = mutableListOf<GearyComponentId>()
    private val _relationValueIds = mutableListOf<RelationValueId>()

    protected fun add(element: FamilyBuilder) {
        elements += element
        if (element is MutableComponentLeaf) {
            val comp = element.component
            _components += comp
            if (comp.holdsData())
                _componentsWithData += comp
        }

        if (element is MutableRelationValueLeaf)
            _relationValueIds += element.relationValueId
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
            has(componentId<T>().withInvertedRole(HOLDS_DATA))
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
            add(MutableComponentLeaf(it))
        }
    }

    public fun hasRelation(key: KType, value: KType) {
        val anyKey = (key.classifier == Any::class)
        val anyValue = (value.classifier == Any::class)
        val relationKey = if (anyKey) null else componentId(key)
        val relationValue = if (anyValue) null else componentId(value)

        when {
            relationKey != null && relationValue != null -> {
                if(key.isMarkedNullable) or {
                    hasRelation(Relation.of(relationKey.withRole(HOLDS_DATA), relationValue))
                    hasRelation(Relation.of(relationKey.withoutRole(HOLDS_DATA), relationValue))
                } else
                    hasRelation(Relation.of(relationKey, relationValue))
            }
            relationValue != null -> hasRelation(key, relationValue)
            relationKey != null -> hasRelation(relationKey, value)
            else -> error("Has relation check cannot be Any to Any yet.")
        }
    }

    public fun hasRelation(key: KType, value: GearyComponentId) {
        // If key is Any, we treat this as matching any key
        if (key.classifier == Any::class)
            add(MutableRelationValueLeaf(RelationValueId(value), !key.isMarkedNullable))
        else hasRelation(Relation.of(componentId(key), value))
    }

    public fun hasRelation(key: GearyComponentId, value: KType) {
        if (value.classifier == Any::class)
            add(MutableRelationKeyLeaf(key))
        else hasRelation(Relation.of(key, componentId(value)))
    }

    public fun hasRelation(relation: Relation) {
        has(relation.id)
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
