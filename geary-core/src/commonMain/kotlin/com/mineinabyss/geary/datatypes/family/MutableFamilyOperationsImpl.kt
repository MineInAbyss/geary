package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.components.events.AddedComponent
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.MutableFamilyOperations.Companion.or
import com.mineinabyss.geary.helpers.componentId
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public class MutableFamilyOperationsImpl : MutableFamilyOperations {
    override val elements: MutableList<Family> = mutableListOf()

    private var onAdd: MutableFamily.Selector.Or? = null

    public override val components: List<GearyComponentId> get() = _components
    public override val componentsWithData: List<GearyComponentId> get() = _componentsWithData
    public override val relationValueIds: List<RelationValueId> get() = _relationValueIds

    private val _components = mutableListOf<GearyComponentId>()
    private val _componentsWithData = mutableListOf<GearyComponentId>()
    private val _relationValueIds = mutableListOf<RelationValueId>()

    override fun add(element: Family) {
        elements += element
        when (element) {
            is MutableFamily.Leaf.Component -> {
                val comp = element.component
                _components += comp
                if (comp.holdsData()) _componentsWithData += comp
            }
            is MutableFamily.Leaf.RelationValue -> _relationValueIds += element.relationValueId
            else -> {}
        }
    }

    override fun has(id: GearyComponentId) {
        add(MutableFamily.Leaf.Component(id))
    }

    override fun hasSet(id: GearyComponentId) {
        has(id.withRole(HOLDS_DATA))
    }

    public override fun hasRelation(key: KType, value: KType) {
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

    public override fun hasRelation(key: KType, value: GearyComponentId) {
        // If key is Any, we treat this as matching any key
        if (key.classifier == Any::class)
            add(MutableFamily.Leaf.RelationValue(RelationValueId(value), !key.isMarkedNullable))
        else hasRelation(Relation.of(componentId(key), value))
    }

    public override fun hasRelation(key: GearyComponentId, value: KType) {
        if (value.classifier == Any::class)
            add(MutableFamily.Leaf.RelationKey(key))
        else hasRelation(Relation.of(key, componentId(value)))
    }

    public override fun hasRelation(relation: Relation) {
        has(relation.id)
    }

    public override fun onAdded(id: GearyComponentId) {
        (onAdd ?: MutableFamily.Selector.Or().also {
            onAdd = it
            add(it)
        }).apply {
            hasRelation(id, typeOf<AddedComponent>())
        }
    }
}
