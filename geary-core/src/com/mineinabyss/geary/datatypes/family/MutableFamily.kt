package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.components.ReservedComponents
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.id
import com.mineinabyss.geary.engine.idWithNullable
import com.mineinabyss.geary.modules.Geary

inline fun family(init: MutableFamily.Selector.And.() -> Unit): Family.Selector.And {
    return MutableFamily.Selector.And().apply(init)
}

sealed interface MutableFamily : Family {
//    val comp: ComponentProvider

    sealed interface Leaf : MutableFamily {
        data class Component(
            override var component: ComponentId,
        ) : Leaf, Family.Leaf.Component

        data class AnyToTarget(
            override var target: EntityId,
            override val kindMustHoldData: Boolean,
        ) : Leaf, Family.Leaf.AnyToTarget

        data class KindToAny(
            override var kind: ComponentId,
            override val targetMustHoldData: Boolean,
        ) : Leaf, Family.Leaf.KindToAny
    }


    sealed class Selector : MutableFamily, Family.Selector {
        class And(
            and: MutableList<MutableFamily> = mutableListOf(),
        ) : Selector(), Family.Selector.And {
            init {
                elements.addAll(and)
            }

            override val and: List<Family> get() = elements
        }

        class AndNot(
            andNot: MutableList<MutableFamily> = mutableListOf(),
        ) : Selector(), Family.Selector.AndNot {
            init {
                elements.addAll(andNot)
            }

            override val andNot: List<Family> get() = elements
        }

        class Or(
            or: MutableList<MutableFamily> = mutableListOf(),
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
            val any = ReservedComponents.ANY
            val specificKind = kind and ENTITY_MASK != any
            val specificTarget = target and ENTITY_MASK != any
            return when {
                specificKind && specificTarget -> has(Relation.of(kind, target).id)
                specificTarget -> add(Leaf.AnyToTarget(target, kind.holdsData()))
                specificKind -> add(Leaf.KindToAny(kind, target.holdsData()))
                else -> error("Has relation check cannot be Any to Any yet.")
            }
        }

        inline fun <reified K, reified T> Geary.hasRelation(): Unit = hasRelation<K>(componentProvider.idWithNullable<T>())

        inline fun <reified K> Geary.hasRelation(target: EntityId) {
            val kind = componentProvider.idWithNullable<K>()
            hasRelation(kind, target)
        }

        inline fun <reified K> Geary.hasRelation(target: Entity): Unit = hasRelation<K>(target.id)


        inline fun or(init: Or.() -> Unit) {
            add(Or().apply(init))
        }

        inline fun and(init: And.() -> Unit) {
            add(And().apply(init))
        }

        inline fun not(init: AndNot.() -> Unit) {
            add(AndNot().apply(init))
        }

        inline fun <reified T : Component> Geary.has(): Unit =
            has(componentProvider.id<T>())

        inline fun <reified T : Component> Geary.hasSet(): Unit =
            hasSet(componentProvider.id<T>())

        fun has(vararg componentIds: ComponentId) {
            has(componentIds)
        }

        fun has(componentIds: Collection<ComponentId>) {
            componentIds.forEach(::has)
        }
    }
}
