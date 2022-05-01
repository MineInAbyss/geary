package com.mineinabyss.geary.datatypes.family


import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.componentId
import kotlin.reflect.KType

public interface MutableFamilyOperations {

    public val elements: MutableList<Family>

    public val components: List<GearyComponentId>
    public val componentsWithData: List<GearyComponentId>
    public val relationValueIds: List<RelationValueId>

    public fun add(element: Family)

    public fun has(id: GearyComponentId)
    public fun has(vararg ids: GearyComponentId) {
        ids.forEach { has(it) }
    }

    public fun hasSet(id: GearyComponentId)

    public fun hasRelation(key: KType, value: KType)
    public fun hasRelation(key: KType, value: GearyComponentId)
    public fun hasRelation(key: GearyComponentId, value: KType)
    public fun hasRelation(relation: Relation)

    public fun onAdded(id: GearyComponentId)

    public companion object {
        public inline fun MutableFamilyOperations.or(init: MutableFamily.Selector.Or.() -> Unit) {
            add(MutableFamily.Selector.Or().apply(init))
        }

        public inline fun MutableFamilyOperations.and(init: MutableFamily.Selector.And.() -> Unit) {
            add(MutableFamily.Selector.And().apply(init))
        }

        public inline fun MutableFamilyOperations.not(init: MutableFamily.Selector.AndNot.() -> Unit) {
            add(MutableFamily.Selector.AndNot().apply(init))
        }

        public inline fun <reified T : GearyComponent> MutableFamilyOperations.has(): Unit =
            or {
                val id = componentId<T>()
                has(id)
                has(id.withInvertedRole(HOLDS_DATA))
            }

        public inline fun <reified T : GearyComponent> MutableFamilyOperations.hasAdded(): Unit =
            has(componentId<T>())

        public inline fun <reified T : GearyComponent> MutableFamilyOperations.hasSet(): Unit =
            hasSet(componentId<T>())

        public fun MutableFamilyOperations.has(vararg componentIds: GearyComponentId) {
            has(componentIds)
        }

        public fun MutableFamilyOperations.has(componentIds: Collection<GearyComponentId>) {
            componentIds.forEach(::has)
        }
    }
}
