package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.datatypes.GearyEntityId

public sealed interface Family {
    public sealed class Leaf : Family {
        public sealed interface Component : Family {
            public val component: GearyComponentId
        }

        public sealed interface RelationTarget : Family {
            public val relationTargetId: GearyEntityId
            public val componentMustHoldData: Boolean
        }

        public sealed interface RelationKind : Family {
            public val relationKindId: GearyComponentId
            public val componentMustHoldData: Boolean
        }
    }

    public sealed interface Selector : Family {
        public val components: List<GearyComponentId>
        public val componentsWithData: List<GearyComponentId>
        public val relationTargetIds: List<GearyEntityId>

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
