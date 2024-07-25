package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.prefabs.configuration.components.EventBind.CachedEvent
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlin.jvm.JvmInline

@Serializable(with = EntityObservers.Serializer::class)
class EntityObservers(val observers: List<EventBind>) {
    class Serializer : InnerSerializer<Map<SerializableComponentId, List<SerializedComponents>>, EntityObservers>(
        serialName = "geary:observe",
        inner = MapSerializer(
            SerializableComponentId.serializer(),
            ListSerializer(PolymorphicListAsMapSerializer.ofComponents())
        ),
        inverseTransform = { it.observers.associate { it.event to it.emit } },
        transform = { EntityObservers(it.map { (event, emit) -> EventBind(event, emit = emit) }) }
    )
}


@Serializable
abstract class Expression<T> {
    abstract fun evaluate(context: RoleContext): T
}


@JvmInline
@Serializable
value class EntityExpression(
    val expression: String,
) /*: Expression<GearyEntity>()*/ {
    fun evaluate(context: RoleContext): GearyEntity {
        return if (expression == "parent") context.entity.parent!!
        else TODO()
    }
}

class RoleContext(
    var entity: GearyEntity,
) {
    fun <T> eval(expression: Expression<T>): T = expression.evaluate(this)
}

interface Action {
    fun RoleContext.execute()
}

interface Condition {
    fun RoleContext.execute(): Boolean
}


class RoleCancelledException : Exception()

@Serializable(with = BecomeAction.Serializer::class)
@SerialName("geary:become")
class BecomeAction(
    val become: EntityExpression,
) : Action {
    override fun RoleContext.execute() {
        entity = become.evaluate(this)
    }

    object Serializer : InnerSerializer<EntityExpression, BecomeAction>(
        serialName = "geary:become",
        inner = EntityExpression.serializer(),
        inverseTransform = { it.become },
        transform = ::BecomeAction
    )
}


@Serializable(with = EnsureAction.Serializer::class)
class EnsureAction(
    val conditions: SerializedComponents,
) : Action {
    @Transient
    private val flat = conditions.map { CachedEvent(componentId(it::class), it) }

    override fun RoleContext.execute() {
        flat.forEach {
            when (val condition = it.data) {
                is Condition -> with(condition) {
                    if(!execute()) throw RoleCancelledException()
                }
                else -> entity.emit(it.componentId, it.data) //TODO use geary condition system if we get one
            }
        }
    }

    object Serializer: InnerSerializer<SerializedComponents, EnsureAction>(
        serialName = "geary:ensure",
        inner = PolymorphicListAsMapSerializer.ofComponents(),
        inverseTransform = { it.conditions },
        transform = { EnsureAction(it) }
    )
}

class EventBind(
    val event: SerializableComponentId,
    val involving: List<SerializableComponentId> = listOf(),
    val emit: List<SerializedComponents>,
) {
    class CachedEvent(val componentId: ComponentId, val data: Any?)

    @Transient
    val emitEvents = emit.flatten().map { CachedEvent(componentId(it::class), it) }
}
