package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.ComponentProvider
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.id
import com.mineinabyss.geary.engine.idWithNullable
import com.mineinabyss.geary.modules.Geary

inline fun Geary.family(init: MutableFamily.Selector.And.() -> Unit): Family.Selector.And {
    return family(module.componentProvider, init)
}

inline fun family(comp: ComponentProvider, init: MutableFamily.Selector.And.() -> Unit): Family.Selector.And {
    return MutableFamily.Selector.And(comp).apply(init)
}

sealed interface MutableFamily : Family {
    val comp: ComponentProvider

    sealed interface Leaf : MutableFamily {
        data class Component(
            override val comp: ComponentProvider,
            override var component: ComponentId,
        ) : Leaf, Family.Leaf.Component

        data class AnyToTarget(
            override val comp: ComponentProvider,
            override var target: EntityId,
            override val kindMustHoldData: Boolean,
        ) : Leaf, Family.Leaf.AnyToTarget

        data class KindToAny(
            override val comp: ComponentProvider,
            override var kind: ComponentId,
            override val targetMustHoldData: Boolean,
        ) : Leaf, Family.Leaf.KindToAny
    }


    sealed class Selector : MutableFamily, Family.Selector {
        class And(
            override val comp: ComponentProvider,
            and: MutableList<MutableFamily> = mutableListOf(),
        ) : Selector(), Family.Selector.And {
            init {
                elements.addAll(and)
            }

            override val and: List<Family> get() = elements
        }

        class AndNot(
            override val comp: ComponentProvider,
            andNot: MutableList<MutableFamily> = mutableListOf(),
        ) : Selector(), Family.Selector.AndNot {
            init {
                elements.addAll(andNot)
            }

            override val andNot: List<Family> get() = elements
        }

        class Or(
            override val comp: ComponentProvider,
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
            add(Leaf.Component(comp, id))
        }

        fun hasSet(id: ComponentId) {
            has(id.withRole(HOLDS_DATA))
        }

        /** Matches against relations using same rules as [Archetype.getRelations] */
        fun hasRelation(
            kind: ComponentId,
            target: EntityId,
        ) {
            val specificKind = kind and ENTITY_MASK != comp.types.any
            val specificTarget = target and ENTITY_MASK != comp.types.any
            return when {
                specificKind && specificTarget -> has(Relation.of(kind, target).id)
                specificTarget -> add(Leaf.AnyToTarget(comp, target, kind.holdsData()))
                specificKind -> add(Leaf.KindToAny(comp, kind, target.holdsData()))
                else -> error("Has relation check cannot be Any to Any yet.")
            }
        }

        inline fun <reified K, reified T> hasRelation(): Unit = hasRelation<K>(comp.idWithNullable<T>())

        inline fun <reified K> hasRelation(target: EntityId) {
            val kind = comp.idWithNullable<K>()
            hasRelation(kind, target)
        }

        inline fun <reified K> hasRelation(target: Entity): Unit = hasRelation<K>(target.id)


        inline fun or(init: Or.() -> Unit) {
            add(Or(comp).apply(init))
        }

        inline fun and(init: And.() -> Unit) {
            add(And(comp).apply(init))
        }

        inline fun not(init: AndNot.() -> Unit) {
            add(AndNot(comp).apply(init))
        }

        inline fun <reified T : Component> has(): Unit =
            has(comp.id<T>())

        inline fun <reified T : Component> hasSet(): Unit =
            hasSet(comp.id<T>())

        fun has(vararg componentIds: ComponentId) {
            has(componentIds)
        }

        fun has(componentIds: Collection<ComponentId>) {
            componentIds.forEach(::has)
        }
    }
}
