package com.mineinabyss.geary.datatypes

/**
 * Maps lower 32 bits of entities to their full 64bit entity id which contains the currently living version.
 * Can be used to check whether an entity with its version is alive.
 */
class EntityIndex {
    private val entityIds = SparseSet<Long>()
    private var last = 1L

    fun isAlive(entity: Long): Boolean {
        return entityIds[entity.toInt()] == entity
    }


    //TODO entity reuse
    fun new(): Long {
        return last++
    }
}