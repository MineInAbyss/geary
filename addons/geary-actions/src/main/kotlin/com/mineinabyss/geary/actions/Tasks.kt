package com.mineinabyss.geary.actions

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = Tasks.Serializer::class)
class Tasks(
    val tasks: List<@Serializable(with = TaskActionByNameSerializer::class) Task<Action>>,
) : Action {
    override fun ActionGroupContext.execute() {
        tasks.forEach { task ->
            try {
                task.execute(this)
            } catch (_: ActionsCancelledException) {
                return
            }
        }
    }

    object Serializer : InnerSerializer<List<Task<Action>>, Tasks>(
        "geary:run",
        ListSerializer(TaskActionByNameSerializer),
        { Tasks(it) },
        { it.tasks }
    )
}
