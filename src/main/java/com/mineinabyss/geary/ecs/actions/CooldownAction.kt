package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.CooldownManager
import com.mineinabyss.geary.ecs.components.getOrAddPersisting
import com.mineinabyss.idofront.time.TimeSpan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("cooldown")
public class CooldownAction(
        private val length: TimeSpan,
        private val run: List<GearyAction>,
        @SerialName("name")
        public val _name: String? = null
) : GearyAction() {
    @Transient
    private val name = _name ?: run.hashCode().toString()

    override fun runOn(entity: GearyEntity) {
        val cooldowns = entity.getOrAddPersisting { CooldownManager() }
        cooldowns.onCooldown(name, length.millis) {
            run.forEach { it.runOn(entity) }
        }
    }
}
