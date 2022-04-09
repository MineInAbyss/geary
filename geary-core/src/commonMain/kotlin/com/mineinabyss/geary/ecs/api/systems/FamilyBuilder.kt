package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.*
import com.mineinabyss.geary.ecs.events.AddedComponent
import com.mineinabyss.geary.ecs.query.*
import org.koin.core.component.inject
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public abstract class FamilyBuilder<T : Family> {
    public fun build(): T {
        preBuild()
        return buildFamily()
    }

    protected abstract fun buildFamily(): T

    protected open fun preBuild() {}

    public operator fun not(): MutableAndNotSelector = MutableAndNotSelector(mutableListOf(this))
}

public inline fun family(init: MutableAndSelector.() -> Unit): AndSelector {
    return MutableAndSelector().apply(init).build()
}

public class MutableComponentLeaf(
    public var component: GearyComponentId
) : FamilyBuilder<ComponentLeaf>() {
    override fun buildFamily(): ComponentLeaf = ComponentLeaf(component)
}

public class MutableRelationValueLeaf(
    public var relationValueId: RelationValueId,
    public val componentMustHoldData: Boolean = false
) : FamilyBuilder<RelationValueLeaf>() {
    override fun buildFamily(): RelationValueLeaf = RelationValueLeaf(relationValueId, componentMustHoldData)
}

public class MutableRelationKeyLeaf(
    public var relationKeyId: GearyComponentId,
    public val componentMustHoldData: Boolean = false
) : FamilyBuilder<RelationKeyLeaf>() {
    override fun buildFamily(): RelationKeyLeaf = RelationKeyLeaf(relationKeyId)
}


public abstract class MutableSelector<T : Family> : FamilyBuilder<T>(), EngineContext {
    override val engine: Engine by inject()
    protected abstract val elements: MutableList<FamilyBuilder<*>>

    public val components: List<GearyComponentId> get() = _components
    public val componentsWithData: List<GearyComponentId> get() = _componentsWithData
    public val relationValueIds: List<RelationValueId> get() = _relationValueIds
    public val onAdd: MutableSet<GearyComponentId> = mutableSetOf()

    private val _components = mutableListOf<GearyComponentId>()
    private val _componentsWithData = mutableListOf<GearyComponentId>()
    private val _relationValueIds = mutableListOf<RelationValueId>()

    override fun preBuild() {
        if (onAdd.isNotEmpty()) or {
            this@MutableSelector.onAdd.forEach {
                hasRelation(it, typeOf<AddedComponent>())
            }
        }
    }

    @PublishedApi
    internal fun add(element: FamilyBuilder<*>) {
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

    public inline fun not(init: MutableAndNotSelector.() -> Unit) {
        add(MutableAndNotSelector().apply(init))
    }

    public inline fun and(init: MutableAndSelector.() -> Unit) {
        add(MutableAndSelector().apply(init))
    }

    public inline fun or(init: MutableOrSelector.() -> Unit) {
        add(MutableOrSelector().apply(init))
    }

    public inline fun <reified T : GearyComponent> has() {
        or {
            has(componentId<T>())
            has(componentId<T>().withInvertedRole(HOLDS_DATA))
        }
    }

    public inline fun <reified T : GearyComponent> hasAdded() {
        has(componentId<T>())

    }

    public inline fun <reified T : GearyComponent> hasSet() {
        has(componentId<T>().withRole(HOLDS_DATA))
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
                if (key.isMarkedNullable) or {
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

    public fun onAdded(id: GearyComponentId) {
        onAdd += id
    }
}

public open class MutableAndSelector(
    override val elements: MutableList<FamilyBuilder<*>> = mutableListOf()
) : MutableSelector<AndSelector>() {
    override fun buildFamily(): AndSelector = AndSelector(elements.build())
}

public class MutableAndNotSelector(
    override val elements: MutableList<FamilyBuilder<*>> = mutableListOf()
) : MutableSelector<AndNotSelector>() {
    override fun buildFamily(): AndNotSelector = AndNotSelector(elements.build())
}

public class MutableOrSelector(
    override val elements: MutableList<FamilyBuilder<*>> = mutableListOf()
) : MutableSelector<OrSelector>() {
    override fun buildFamily(): OrSelector = OrSelector(elements.build())
}

private fun List<FamilyBuilder<*>>.build() = map { it.build() }
