package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId

public sealed interface Family {
    public sealed class Leaf : Family {
        public sealed interface Component : Family {
            public val component: ComponentId
        }

        public sealed interface AnyToTarget : Family {
            public val target: EntityId
            public val kindMustHoldData: Boolean
        }

        public sealed interface KindToAny : Family {
            public val kind: ComponentId
            public val targetMustHoldData: Boolean
        }
    }

    public sealed interface Selector : Family {
        public val components: List<ComponentId>
        public val componentsWithData: List<ComponentId>

        public sealed interface And : Selector {
            public val and: List<Family>
        }

        public sealed interface AndNot : Selector {
            public val andNot: List<Family>
        }

        public sealed interface Or : Selector {
            public val or: List<Family>
        }
    }
}
