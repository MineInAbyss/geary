package com.mineinabyss.geary.actions

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.yamlMap
import com.mineinabyss.geary.actions.actions.EmitEventAction.Companion.wrapIfNotAction
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.serializers.DurationSerializer
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.getWorld
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

@Serializable
class Task<T : Action>(
    val action: T,
    @SerialName("when")
    val conditions: List<EnsureAction>? = null,
    val register: String? = null,
    val onFail: Tasks? = null,
    @SerialName("with")
    val environmentOverrides: Map<String, Expression<@Contextual Any>>? = null,
    val loop: Expression<List<@Contextual Any>>? = null,
    val repeat: Int = 1,
    val repeatInterval: @Serializable(with = DurationSerializer::class) Duration = Duration.ZERO,
) {
    fun execute(context: ActionGroupContext) {
        if (repeatInterval == Duration.ZERO) repeat(repeat) {
            executeLooping(context)
        } else {
            context.coroutineScope?.launch {
                repeat(repeat) {
                    if (context.entity?.exists() == false) return@launch
                    executeLooping(context)
                    delay(repeatInterval)
                }
            }
        }
    }

    private fun executeLooping(context: ActionGroupContext) = with(context) {
        val context = if (action.useSubcontext)
            environmentOverrides?.let { env -> this.plus(env.mapValues { eval(it.value) }) } ?: this
        else this
        try {
            if (loop != null) {
                loop.evaluate(context).forEach { loopEntry ->
                    val subcontext = context.copy()
                    subcontext.register("it", loopEntry)
                    executeInLoop(subcontext)
                }
            } else executeInLoop(context)
        } catch (e: ActionsCancelledException) {
            onFail?.execute(context)
            throw e
        }
    }

    private fun executeInLoop(context: ActionGroupContext) {
        conditions?.forEach { condition ->
            condition.execute(context)
        }

        val returned = action.execute(context)

        if (action is Condition && returned == false) {
            throw ActionsCancelledException()
        }

        if (register != null) context.register(register, returned)
    }
}

// TODO swap to YamlTransformingSerializer when we update kaml
@OptIn(ExperimentalSerializationApi::class)
object TaskActionByNameSerializer : KSerializer<Task<Action>> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    override fun deserialize(decoder: Decoder): Task<Action> {
        val world = decoder.serializersModule.getWorld()
        val yaml = decoder as YamlInput
        val taskDescriptor = Task.serializer(ContextualSerializer(Any::class)).descriptor
        //TODO get yaml node directly when we update kaml
        val node = (yaml.beginStructure(ContextualSerializer(Any::class).descriptor) as YamlInput).node.yamlMap

        // Find action key and get the serializer based on it
        val action = node.entries.entries
            .singleOrNull { (key, _) -> key.content !in taskDescriptor.elementNames }
            ?: error("Action must be specified by its serial name in task")
        val serialName =
            action.key.content.let { if (!it.contains(":")) "geary:$it" else it }.fromCamelCaseToSnakeCase()

        val actionSerializer = ActionOrComponentAsActionSerializer(
            world.getAddon(SerializableComponents).formats
                .getSerializerFor(serialName, GearyComponent::class)
                ?: error("Serializer for action $serialName not found")
        )

        // Pass back into Task's generated serializer
        val alternedNode = node.copy(entries = node.entries.toMutableMap().apply {
            remove(action.key)
            put(action.key.copy(content = "action"), action.value)
        })
        return yaml.yaml.decodeFromYamlNode(Task.serializer<Action>(actionSerializer), alternedNode)
    }

    override fun serialize(encoder: Encoder, value: Task<Action>) {
        error("Tasks cannot be serialized, only deserialized")
    }
}

class ActionOrComponentAsActionSerializer(
    val actionSerializer: DeserializationStrategy<Any>,
) : KSerializer<Action> {
    override val descriptor: SerialDescriptor = actionSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Action,
    ) = error("Cannot encode actions with this serializer")

    override fun deserialize(decoder: Decoder): Action {
        val world = decoder.serializersModule.getWorld()
        val action = actionSerializer.deserialize(decoder)
        return wrapIfNotAction(world, action)
    }
}
