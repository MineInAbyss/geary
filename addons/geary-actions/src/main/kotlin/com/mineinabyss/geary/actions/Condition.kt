package com.mineinabyss.geary.actions

interface Condition: Action {
    override fun ActionGroupContext.execute(): Boolean

}

fun Condition.execute(context: ActionGroupContext) = context.execute()
