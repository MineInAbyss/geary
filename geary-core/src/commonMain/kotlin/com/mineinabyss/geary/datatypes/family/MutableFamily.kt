package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable
import com.mineinabyss.geary.modules.geary

inline fun family(init: MutableFamily.Selector.And.() -> Unit): Family.Selector.And {
    return MutableFamily.Selector.And().apply(init)
}

sealed class MutableFamily : Family {
    sealed class Leaf : MutableFamily() {
        class Component(
            override var component: ComponentId
        ) : Leaf(), Family.Leaf.Component

        class AnyToTarget(
            override var target: EntityId,
            override val kindMustHoldData: Boolean
        ) : Leaf(), Family.Leaf.AnyToTarget

        class KindToAny(
            override var kind: ComponentId,
            override val targetMustHoldData: Boolean
        ) : Leaf(), Family.Leaf.KindToAny
    }


    sealed class Selector : MutableFamily(), Family.Selector {
        class And(
            and: MutableList<MutableFamily> = mutableListOf()
        ) : Selector(), Family.Selector.And {
            init {
                elements.addAll(and)
            }

            override val and: List<Family> get() = elements
        }

        class AndNot(
            andNot: MutableList<MutableFamily> = mutableListOf()
        ) : Selector(), Family.Selector.AndNot {
            init {
                elements.addAll(andNot)
            }

            override val andNot: List<Family> get() = elements
        }

        class Or(
            or: MutableList<MutableFamily> = mutableListOf()
        ) : Selector(), Family.Selector.Or {
            init {
                elements.addAll(or)
            }

            override val or: List<Family> get() = elements
        }

        val elements: MutableList<Family> = mutableListOf()

        override val components: List<ComponentId> get() = _components
        override val componentsWithData: List<ComponentId> get() = _componentsWithData

        private val _components = mutableListOf<ComponentId>()
        private val _componentsWithData = mutableListOf<ComponentId>()

        fun add(element: Family) {
            elements += element
            when (element) {
                is Leaf.Component -> {
                    val comp = element.component
                    _components += comp
                    if (comp.holdsData()) _componentsWithData += comp
                }

                else -> {}
            }
        }

        fun has(id: ComponentId) {
            add(Leaf.Component(id))
        }

        fun hasSet(id: ComponentId) {
            has(id.withRole(HOLDS_DATA))
        }

        /** Matches against relations using same rules as [Archetype.getRelations] */
        fun hasRelation(
            kind: ComponentId,
            target: EntityId,
        ) {
            val specificKind = kind and ENTITY_MASK != geary.components.any
            val specificTarget = target and ENTITY_MASK != geary.components.any
            return when {
                specificKind && specificTarget -> has(Relation.of(kind, target).id)
                specificTarget -> add(Leaf.AnyToTarget(target, kind.holdsData()))
                specificKind -> add(Leaf.KindToAny(kind, target.holdsData()))
                else -> error("Has relation check cannot be Any to Any yet.")
            }
        }

        inline fun <reified K, reified T> hasRelation(): Unit = hasRelation<K>(componentIdWithNullable<T>())

        inline fun <reified K> hasRelation(target: EntityId) {
            val kind = componentIdWithNullable<K>()
            hasRelation(kind, target)
        }

        inline fun <reified K> hasRelation(target: Entity): Unit = hasRelation<K>(target.id)


        inline fun or(init: Or.() -> Unit) {
            add(Or().apply(init))
        }

        inline fun and(init: And.() -> Unit) {
            add(And().apply(init))
        }

        inline fun not(init: AndNot.() -> Unit) {
            add(AndNot().apply(init))
        }

        inline fun <reified T : Component> has(): Unit =
            has(componentId<T>())

        inline fun <reified T : Component> hasSet(): Unit =
            hasSet(componentId<T>())

        fun has(vararg componentIds: ComponentId) {
            has(componentIds)
        }

        fun has(componentIds: Collection<ComponentId>) {
            componentIds.forEach(::has)
        }
    }
}
