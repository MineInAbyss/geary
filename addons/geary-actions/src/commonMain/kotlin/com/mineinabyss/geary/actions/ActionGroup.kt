package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.actions.EnsureAction

class ActionEntry(
    val action: Action,
    val conditions: List<EnsureAction>?,
    val register: String?,
)

class ActionGroup(
    val actions: List<ActionEntry>,
) {
    fun execute(context: ActionGroupContext) {
        actions.forEach { entry ->
            try {
                entry.conditions?.forEach { condition ->
                    with(condition) { context.execute() }
                }

                val returned = with(entry.action) { context.execute() }

                if (entry.register != null)
                    context.register(entry.register, returned)
            } catch (e: ActionsCancelledException) {
                return
            }
        }
    }
}
