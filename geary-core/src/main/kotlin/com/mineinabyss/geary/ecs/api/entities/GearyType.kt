package com.mineinabyss.geary.ecs.api.entities

@JvmInline
public value class GearyType(public val longArray: LongArray) {
    public fun get(index: Int): GearyEntity = longArray[index].toGeary()
    public fun set(index: Int, gearyEntity: GearyEntity) {
        longArray[index] = gearyEntity.id.toLong()
    }

    public inline fun forEach(run: (GearyEntity) -> Unit) {
        longArray.forEach { run(it.toGeary()) }
    }
}
