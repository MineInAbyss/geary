package com.mineinabyss.geary.actions

import kotlin.jvm.JvmName

interface Action {
    fun ActionGroupContext.execute(): Any?

}

fun Action.execute(context: ActionGroupContext) = context.execute()
