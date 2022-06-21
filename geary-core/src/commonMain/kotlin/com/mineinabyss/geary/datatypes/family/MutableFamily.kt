package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.components.events.AddedComponent
import com.mineinabyss.geary.components.events.SetComponent
import com.mineinabyss.geary.components.events.UpdatedComponent
import com.mineinabyss.geary.context.globalContext
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
            override var component: GearyComponentId
        ) : Leaf(), Family.Leaf.Component

        public class AnyToTarget(
            public override var target: GearyEntityId,
            public override val kindMustHoldData: Boolean
        ) : Leaf(), Family.Leaf.AnyToTarget

        public class KindToAny(
            public override var kind: GearyComponentId,
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

        public override val components: List<GearyComponentId> get() = _components
        public override val componentsWithData: List<GearyComponentId> get() = _componentsWithData

        private val _components = mutableListOf<GearyComponentId>()
        private val _componentsWithData = mutableListOf<GearyComponentId>()

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

        public fun has(id: GearyComponentId) {
            add(Leaf.Component(id))
        }

        public fun hasSet(id: GearyComponentId) {
            has(id.withRole(HOLDS_DATA))
        }

        /** Matches against relations using same rules as [Archetype.getRelations] */
        public fun hasRelation(
            kind: GearyComponentId,
            target: GearyEntityId,
        ) {
            val specificKind = kind and ENTITY_MASK != globalContext.components.any
            val specificTarget = target and ENTITY_MASK != globalContext.components.any
            return when {
                specificKind && specificTarget -> has(Relation.of(kind, target).id)
                specificTarget -> add(Leaf.AnyToTarget(target, kind.holdsData()))
                specificKind -> add(Leaf.KindToAny(kind, target.holdsData()))
                else -> error("Has relation check cannot be Any to Any yet.")
            }
        }

        public inline fun <reified K, reified T> hasRelation(): Unit = hasRelation<K>(componentIdWithNullable<T>())

        public inline fun <reified K> hasRelation(target: GearyEntityId) {
            val kind = componentIdWithNullable<K>()
            hasRelation(kind, target)
        }

        public inline fun <reified K> hasRelation(target: GearyEntity): Unit = hasRelation<K>(target.id)

        public fun onAdd(id: GearyComponentId) {
            onAdd.hasRelation<AddedComponent?>(id)
        }

        public fun onSet(id: GearyComponentId) {
            onAdd.hasRelation<UpdatedComponent?>(id)
            onAdd.hasRelation<SetComponent?>(id)
        }

        public fun onFirstSet(id: GearyComponentId) {
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

        public inline fun <reified T : GearyComponent> has(): Unit =
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
