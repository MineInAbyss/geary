package com.mineinabyss.geary.systems.accessors

public interface Accessor<out T> {
    public fun access(scope: ResultScope): T
}
