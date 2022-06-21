package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.datatypes.GearyEntityId

public sealed interface Family {
    public sealed class Leaf : Family {
        public sealed interface Component : Family {
            public val component: GearyComponentId
        }

        public sealed interface AnyToTarget : Family {
            public val target: GearyEntityId
            public val kindMustHoldData: Boolean
        }

        public sealed interface KindToAny : Family {
            public val kind: GearyComponentId
            public val targetMustHoldData: Boolean
        }
    }

    public sealed interface Selector : Family {
        public val components: List<GearyComponentId>
        public val componentsWithData: List<GearyComponentId>

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
