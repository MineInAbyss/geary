package com.mineinabyss.geary.actions

import kotlin.jvm.JvmName

interface Action {
    /** Should this action create a copy of [ActionGroupContext] to run or not? */
    val useSubcontext: Boolean get() = true

    fun ActionGroupContext.execute(): Any?

}

fun Action.execute(context: ActionGroupContext) = context.execute()
