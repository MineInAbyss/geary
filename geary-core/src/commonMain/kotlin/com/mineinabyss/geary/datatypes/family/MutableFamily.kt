package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.components.events.AddedComponent
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.componentId
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public inline fun family(init: MutableFamily.Selector.And.() -> Unit): Family {
    return MutableFamily.Selector.And().apply(init)
}

public sealed class MutableFamily : Family {
    public sealed class Leaf : MutableFamily() {
        public class Component(
            override var component: GearyComponentId
        ) : Leaf(), Family.Leaf.Component

        public class RelationValue(
            public override var relationTargetId: GearyEntityId,
            public override val componentMustHoldData: Boolean = false
        ) : Leaf(), Family.Leaf.RelationValue

        public class RelationKey(
            public override var relationKeyId: GearyComponentId,
            public override val componentMustHoldData: Boolean = false
        ) : Leaf(), Family.Leaf.RelationKey
    }


    public sealed class Selector : MutableFamily(), Family.Selector {
        public class And(
            and: MutableList<MutableFamily> = mutableListOf()
        ) : Selector(), Family.Selector.And {
            init {
                elements.addAll(and)
            }

            override val and: List<Family> get() = elements
        }

        public class AndNot(
            andNot: MutableList<MutableFamily> = mutableListOf()
        ) : Selector(), Family.Selector.AndNot {
            init {
                elements.addAll(andNot)
            }

            override val andNot: List<Family> get() = elements
        }

        public class Or(
            or: MutableList<MutableFamily> = mutableListOf()
        ) : Selector(), Family.Selector.Or {
            init {
                elements.addAll(or)
            }

            override val or: List<Family> get() = elements
        }

        public val elements: MutableList<Family> = mutableListOf()

        private var onAdd: Or? = null

        public override val components: List<GearyComponentId> get() = _components
        public override val componentsWithData: List<GearyComponentId> get() = _componentsWithData
        public override val relationTargetIds: List<GearyEntityId> get() = _relationValueIds

        private val _components = mutableListOf<GearyComponentId>()
        private val _componentsWithData = mutableListOf<GearyComponentId>()
        private val _relationValueIds = mutableListOf<GearyEntityId>()

        public fun add(element: Family) {
            elements += element
            when (element) {
                is Leaf.Component -> {
                    val comp = element.component
                    _components += comp
                    if (comp.holdsData()) _componentsWithData += comp
                }
                is Leaf.RelationValue -> _relationValueIds += element.relationTargetId
                else -> {}
            }
        }

        public fun has(id: GearyComponentId) {
            add(Leaf.Component(id))
        }

        public fun hasSet(id: GearyComponentId) {
            has(id.withRole(HOLDS_DATA))
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
                add(Leaf.RelationValue(value, !key.isMarkedNullable))
            else hasRelation(Relation.of(componentId(key), value))
        }

        public fun hasRelation(key: GearyComponentId, value: KType) {
            if (value.classifier == Any::class)
                add(Leaf.RelationKey(key))
            else hasRelation(Relation.of(key, componentId(value)))
        }

        public inline fun <reified K> hasRelation(target: GearyEntity) {
            TODO()
        }

        public fun hasRelation(relation: Relation) {
            has(relation.id)
        }

        public fun onAdded(id: GearyComponentId) {
            (onAdd ?: Or().also {
                onAdd = it
                add(it)
            }).apply {
                hasRelation(id, typeOf<AddedComponent>())
            }
        }

        public inline fun or(init: Or.() -> Unit) {
            add(Or().apply(init))
        }

        public inline fun and(init: And.() -> Unit) {
            add(And().apply(init))
        }

        public inline fun not(init: AndNot.() -> Unit) {
            add(AndNot().apply(init))
        }

        public inline fun <reified T : GearyComponent> has(): Unit =
            or {
                val id = componentId<T>()
                has(id)
                has(id.withInvertedRole(HOLDS_DATA))
            }

        public inline fun <reified T : GearyComponent> hasAdded(): Unit =
            has(componentId<T>())

        public inline fun <reified T : GearyComponent> hasSet(): Unit =
            hasSet(componentId<T>())

        public fun has(vararg componentIds: GearyComponentId) {
            has(componentIds)
        }

        public fun has(componentIds: Collection<GearyComponentId>) {
            componentIds.forEach(::has)
        }
    }
}
