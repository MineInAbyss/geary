package com.mineinabyss.geary.ecs.accessors

//TODO make test for this accessor
public open class ListAccessor<T, A: Accessor<T>>(
    private val accessor: A,
//    index: Int,
//    keyIsNullable: Boolean,
//    relationValue: RelationDataType?,
//    relationKey: GearyComponentId?
) : Accessor<List<T>>(accessor.index) {
    override fun RawAccessorDataScope.readData(): List<List<T>> = listOf(accessor.run { readData() })
}
