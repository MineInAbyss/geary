package com.mineinabyss.geary.ecs.accessors

//TODO make test for this accessor
public open class ListAccessor<T, A : Accessor<T>>(
    private val accessor: A
) : Accessor<List<T>>(accessor.index) {
    init {
        _cached.addAll(accessor.cached)
    }

    override fun RawAccessorDataScope.readData(): List<List<T>> = listOf(accessor.run { readData() })
}
