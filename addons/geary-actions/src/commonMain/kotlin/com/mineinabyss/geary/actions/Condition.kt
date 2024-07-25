package com.mineinabyss.geary.actions

interface Condition {
    fun ActionGroupContext.execute(): Boolean
}
