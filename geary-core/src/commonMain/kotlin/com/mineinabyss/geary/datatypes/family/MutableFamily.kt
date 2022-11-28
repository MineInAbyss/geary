package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.components.events.AddedComponent
import com.mineinabyss.geary.components.events.SetComponent
import com.mineinabyss.geary.components.events.UpdatedComponent
import com.mineinabyss.geary.context.geary
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.componentIdWithNullable

public inline fun family(init: MutableFamily.Selector.And.() -> Unit): Family {
    return MutableFamily.Selector.And().apply(init)
}

public sealed class MutableFamily : Family {
    public sealed class Leaf : MutableFamily() {
        public class Component(
            override var component: ComponentId
        ) : Leaf(), Family.Leaf.Component

        public class AnyToTarget(
            public override var target: EntityId,
            public override val kindMustHoldData: Boolean
        ) : Leaf(), Family.Leaf.AnyToTarget

        public class KindToAny(
            public override var kind: ComponentId,
            public override val targetMustHoldData: Boolean
        ) : Leaf(), Family.Leaf.KindToAny
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

        private val onAdd: Or by lazy { Or().also { add(it) } }

        public override val components: List<ComponentId> get() = _components
        public override val componentsWithData: List<ComponentId> get() = _componentsWithData

        private val _components = mutableListOf<ComponentId>()
        private val _componentsWithData = mutableListOf<ComponentId>()

        public fun add(element: Family) {
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

        public fun has(id: ComponentId) {
            add(Leaf.Component(id))
        }

        public fun hasSet(id: ComponentId) {
            has(id.withRole(HOLDS_DATA))
        }

        /** Matches against relations using same rules as [Archetype.getRelations] */
        public fun hasRelation(
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

        public inline fun <reified K, reified T> hasRelation(): Unit = hasRelation<K>(componentIdWithNullable<T>())

        public inline fun <reified K> hasRelation(target: EntityId) {
            val kind = componentIdWithNullable<K>()
            hasRelation(kind, target)
        }

        public inline fun <reified K> hasRelation(target: Entity): Unit = hasRelation<K>(target.id)

        public fun onAdd(id: ComponentId) {
            onAdd.hasRelation<AddedComponent?>(id)
        }

        public fun onSet(id: ComponentId) {
            onAdd.hasRelation<UpdatedComponent?>(id)
            onAdd.hasRelation<SetComponent?>(id)
        }

        public fun onFirstSet(id: ComponentId) {
            onAdd.hasRelation<SetComponent?>(id)
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

        public inline fun <reified T : Component> has(): Unit =
            has(componentId<T>())

        public inline fun <reified T : Component> hasSet(): Unit =
            hasSet(componentId<T>())

        public fun has(vararg componentIds: ComponentId) {
            has(componentIds)
        }

        public fun has(componentIds: Collection<ComponentId>) {
            componentIds.forEach(::has)
        }
    }
}
