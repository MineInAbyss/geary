package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.datatypes.RelationValueId

public sealed interface Family {
    public sealed class Leaf : Family {
        public sealed interface Component : Family {
            public val component: GearyComponentId
        }

        public sealed interface RelationKey : Family {
            public val relationKeyId: GearyComponentId
            public val componentMustHoldData: Boolean
        }

        public sealed interface RelationValue : Family {
            public val relationValueId: RelationValueId
            public val componentMustHoldData: Boolean
        }
    }

    public sealed interface Selector : Family {
        public val components: List<GearyComponentId>
        public val componentsWithData: List<GearyComponentId>
        public val relationValueIds: List<RelationValueId>

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
