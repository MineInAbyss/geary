package com.mineinabyss.geary.systems.accessors

interface Accessor<out T> {
    fun access(scope: ResultScope): T
}
