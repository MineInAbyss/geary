package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.datatypes.RelationValueId

public inline fun family(init: MutableFamily.Selector.And.() -> Unit): Family {
    return MutableFamily.Selector.And().apply(init)
}

public sealed class MutableFamily : Family {
    public sealed class Leaf : MutableFamily() {
        public class Component(
            override var component: GearyComponentId
        ) : Leaf(), Family.Leaf.Component

        public class RelationValue(
            public override var relationValueId: RelationValueId,
            public override val componentMustHoldData: Boolean = false
        ) : Leaf(), Family.Leaf.RelationValue

        public class RelationKey(
            public override var relationKeyId: GearyComponentId,
            public override val componentMustHoldData: Boolean = false
        ) : Leaf(), Family.Leaf.RelationKey
    }


    public sealed class Selector : MutableFamily(),
        Family.Selector,
        MutableFamilyOperations by MutableFamilyOperationsImpl() {
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

    }
}
