package com.mineinabyss.geary.actions

class ActionGroup(
    val actions: List<Action>,
) {
    fun execute(context: ActionGroupContext) {
        actions.forEach {
            try {
                with(it) { context.execute() }
            } catch (e: ActionsCancelledException) {
                return
            }
        }
    }
}
