package com.mineinabyss.geary.datatypes.family

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

sealed interface Family : ReadOnlyAccessor<Family>, FamilyMatching {

    sealed class Leaf : Family {
        sealed interface Component : Family {
            val component: ComponentId
        }

        sealed interface AnyToTarget : Family {
            val target: EntityId
            val kindMustHoldData: Boolean
        }

        sealed interface KindToAny : Family {
            val kind: ComponentId
            val targetMustHoldData: Boolean
        }
    }

    sealed interface Selector : Family {
        val components: List<ComponentId>
        val componentsWithData: List<ComponentId>

        sealed interface And : Selector {
            val and: List<Family>
        }

        sealed interface AndNot : Selector {
            val andNot: List<Family>
        }

        sealed interface Or : Selector {
            val or: List<Family>
        }
    }

    // Helpers for writing queries
    override val family: Family? get() = this
    override fun getValue(thisRef: Query, property: KProperty<*>): Family {
        return this
    }
}
