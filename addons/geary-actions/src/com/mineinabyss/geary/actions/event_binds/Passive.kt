package com.mineinabyss.geary.actions.event_binds;

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.actions.serializers.DurationSerializer
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import com.mineinabyss.geary.systems.query.query
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
class SystemBind(
    val match: List<SerializableComponentId>,
    val every: @Serializable(with = DurationSerializer::class) Duration = 1.seconds,
    val run: Tasks,
)

@Serializable(with = Passive.Serializer::class)
class Passive(
    val systems: List<SystemBind>,
) {
    object Serializer : InnerSerializer<List<SystemBind>, Passive>(
        serialName = "geary:passive",
        inner = ListSerializer(SystemBind.serializer()),
        inverseTransform = Passive::systems,
        transform = { Passive(it) }
    )
}

fun WorldScoped.parsePassive() = observe<OnSet>()
    .involving(query<Passive>())
    .exec { (passive) ->
        passive.systems.forEach { systemBind ->
            val systemMatchingId = entity().id
            entity.add(systemMatchingId)
            system(query {
                has(systemMatchingId)
                has(systemBind.match)
            }).every(systemBind.every).execOnAll {
                entities().fastForEach { entity ->
                    runCatching {
                        val context = ActionGroupContext(entity)
                        systemBind.run.execute(context)
                    }.onFailure {
                        logger.e { "Error while executing passive system on entity $entity:" }
                        it.printStackTrace()
                    }
                }
            }
        }
        entity.remove<Passive>()
    }

